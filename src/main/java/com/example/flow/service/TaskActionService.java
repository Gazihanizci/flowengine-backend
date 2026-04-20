package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskActionService {

    private final SurecAdimRepository surecAdimRepository;
    private final SurecRepository surecRepository;
    private final AdimGecisKuralRepository gecisRepository;
    private final AkisAdimRepository akisAdimRepository;

    private final FormService formService;
    private final TaskService taskService;
    private final WorkflowEngineService workflowEngineService;

    private final SurecHareketRepository surecHareketRepository;
    private final PdfReportService pdfReportService;

    @Transactional
    public void handleAction(Long taskId, Long aksiyonId, Map<Long, String> formData) {

        if (aksiyonId == null || (aksiyonId != 1 && aksiyonId != 2 && aksiyonId != 3)) {
            throw new RuntimeException("Geçersiz aksiyon");
        }

        SurecAdim currentTask = surecAdimRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task bulunamadı"));

        Long surecId = currentTask.getSurecId();
        Long adimId = currentTask.getAdimId();
        Long userId = currentTask.getAtananKullaniciId();

        // 🔹 KAYDET
        if (aksiyonId == 2) {
            formService.saveFormDraft(surecId, userId, formData);
            return;
        }

        // 🔹 RED
        if (aksiyonId == 3) {

            currentTask.setDurum("REDDEDILDI");
            currentTask.setTamamlandiMi(true);
            currentTask.setBitisTarihi(LocalDateTime.now());
            surecAdimRepository.save(currentTask);

            AkisSurec surec = surecRepository.findById(surecId).orElseThrow();
            surec.setDurum("REDDEDILDI");
            surec.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(surec);

            workflowEngineService.handleChildRejected(surec);
            return;
        }

        // 🔹 ONAY
        formService.validateAndSaveFormData(surecId, adimId, userId, formData);

        AkisAdim step = akisAdimRepository.findById(adimId).orElseThrow();
        AkisSurec surec = surecRepository.findById(surecId).orElseThrow();

        // 🔥 CHILD FLOW VARSA
        if (Boolean.TRUE.equals(step.getExternalFlowEnabled())
                && step.getExternalFlowId() != null) {

            currentTask.setDurum("TAMAMLANDI");
            currentTask.setTamamlandiMi(true);
            currentTask.setBitisTarihi(LocalDateTime.now());
            surecAdimRepository.save(currentTask);

            SurecHareket hareket = new SurecHareket();
            hareket.setSurecId(surecId);
            hareket.setAdimId(adimId);
            hareket.setAksiyonId(aksiyonId);
            hareket.setYapanKullaniciId(userId);
            hareket.setYapilanIslem("Child flow başlatıldı");
            hareket.setTarih(LocalDateTime.now());
            surecHareketRepository.save(hareket);

            surec.setDurum("WAITING_APPROVAL");
            surecRepository.save(surec);

            workflowEngineService.startExternalFlow(surec, step, taskService);
            return;
        }

        // 🔹 NORMAL ONAY
        SurecHareket hareket = new SurecHareket();
        hareket.setSurecId(surecId);
        hareket.setAdimId(adimId);
        hareket.setAksiyonId(aksiyonId);
        hareket.setYapanKullaniciId(userId);
        hareket.setYapilanIslem("Onaylandı");
        hareket.setTarih(LocalDateTime.now());
        surecHareketRepository.save(hareket);

        currentTask.setDurum("TAMAMLANDI");
        currentTask.setTamamlandiMi(true);
        currentTask.setBitisTarihi(LocalDateTime.now());
        surecAdimRepository.save(currentTask);

        // 🔹 PARALLEL CHECK
        boolean childFlow = surec.getParentSurecId() != null;

        if (!childFlow) {
            if (!taskService.isStepFullyCompleted(surecId, adimId)) {
                return;
            }
        }

        // 🔹 NEXT STEP
        Long nextStepId = determineNextStepId(surec, step, aksiyonId);

        if (nextStepId == null) {

            surec.setDurum("TAMAMLANDI");
            surec.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(surec);

            // 🔥 TÜM TASKLARI KAPAT (FIX)
            List<SurecAdim> tasks = surecAdimRepository
                    .findBySurecId(surecId);
            for (SurecAdim t : tasks) {
                if (!Boolean.TRUE.equals(t.getTamamlandiMi())) {
                    t.setDurum("TAMAMLANDI");
                    t.setTamamlandiMi(true);
                    t.setBitisTarihi(LocalDateTime.now());
                    surecAdimRepository.save(t);
                }
            }

            workflowEngineService.resumeParentIfNeeded(surec, taskService);
            return;
        }

        surec.setMevcutAdimId(nextStepId);
        surecRepository.save(surec);
        taskService.createTasksForStep(surecId, nextStepId);
    }

    private Long determineNextStepId(AkisSurec surec, AkisAdim step, Long aksiyonId) {

        List<AdimGecisKural> rules =
                gecisRepository.findByAdimIdAndAksiyonId(step.getAdimId(), aksiyonId);

        if (!rules.isEmpty()) {
            return rules.get(0).getSonrakiAdimId();
        }

        if (aksiyonId == 1) {

            Optional<AkisAdim> next = akisAdimRepository
                    .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                            surec.getAkisId(),
                            step.getAdimSirasi()
                    );

            return next.map(AkisAdim::getAdimId).orElse(null);
        }

        return null;
    }
}