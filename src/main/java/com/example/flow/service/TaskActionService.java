package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public void handleAction(Long taskId, Long aksiyonId, java.util.Map<Long, String> formData) {

        // 🔥 1. AKSİYON KONTROL (3 eklendi)
        if (aksiyonId == null || (aksiyonId != 1 && aksiyonId != 2 && aksiyonId != 3)) {
            throw new RuntimeException("Geçersiz aksiyon (1=Onay, 2=Kaydet, 3=Red)");
        }

        // 🔥 2. TASK BUL
        SurecAdim currentTask = surecAdimRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task bulunamadı: " + taskId));

        Long surecId = currentTask.getSurecId();
        Long adimId = currentTask.getAdimId();
        Long userId = currentTask.getAtananKullaniciId();

        // 🔥 3. KAYDET
        if (aksiyonId == 2) {

            formService.saveFormDraft(
                    surecId,
                    userId,
                    formData
            );

            return;
        }

        // 🔥 4. RED
        if (aksiyonId == 3) {

            // hareket log
            SurecHareket hareket = new SurecHareket();
            hareket.setSurecId(surecId);
            hareket.setAdimId(adimId);
            hareket.setAksiyonId(aksiyonId);
            hareket.setYapanKullaniciId(userId);
            hareket.setYapilanIslem("Talep reddedildi");
            hareket.setTarih(LocalDateTime.now());
            surecHareketRepository.save(hareket);

            // task kapat
            currentTask.setDurum("REDDEDILDI");
            currentTask.setTamamlandiMi(true);
            currentTask.setBitisTarihi(LocalDateTime.now());
            surecAdimRepository.save(currentTask);

            // süreç kapat
            AkisSurec surec = surecRepository.findById(surecId)
                    .orElseThrow(() -> new RuntimeException("Süreç bulunamadı"));

            surec.setDurum("REDDEDILDI");
            surec.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(surec);

            workflowEngineService.handleChildRejected(surec);

            return;
        }

        // 🔥 5. ONAY (1)
        formService.validateAndSaveFormData(
                surecId,
                adimId,
                userId,
                formData
        );

        // 🔥 HAREKET LOG
        SurecHareket hareket = new SurecHareket();
        hareket.setSurecId(surecId);
        hareket.setAdimId(adimId);
        hareket.setAksiyonId(aksiyonId);
        hareket.setYapanKullaniciId(userId);
        hareket.setYapilanIslem("Onaylandı");
        hareket.setTarih(LocalDateTime.now());
        surecHareketRepository.save(hareket);

        // 🔥 TASK TAMAMLA
        currentTask.setDurum("TAMAMLANDI");
        currentTask.setTamamlandiMi(true);
        currentTask.setBitisTarihi(LocalDateTime.now());
        surecAdimRepository.save(currentTask);

        if (!taskService.isStepFullyCompleted(surecId, adimId)) {
            return;
        }

        AkisAdim step = akisAdimRepository.findById(adimId)
                .orElseThrow(() -> new RuntimeException("Adım bulunamadı"));

        AkisSurec surec = surecRepository.findById(surecId)
                .orElseThrow(() -> new RuntimeException("Süreç bulunamadı"));

        if (Boolean.TRUE.equals(step.getExternalFlowEnabled()) && step.getExternalFlowId() != null) {
            workflowEngineService.startExternalFlow(surec, step, taskService);
            return;
        }

        Long nextStepId = determineNextStepId(surec, step, aksiyonId);

        if (nextStepId != null) {
            surec.setMevcutAdimId(nextStepId);
            surecRepository.save(surec);
            taskService.createTasksForStep(surecId, nextStepId);
        }
    }

    private Long determineNextStepId(AkisSurec surec, AkisAdim step, Long aksiyonId) {

        List<AdimGecisKural> rules =
                gecisRepository.findByAdimIdAndAksiyonId(step.getAdimId(), aksiyonId);

        if (!rules.isEmpty()) {
            return rules.get(0).getSonrakiAdimId();
        }

        // 🔥 ONAY
        if (aksiyonId == 1) {

            Optional<AkisAdim> next = akisAdimRepository
                    .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                            surec.getAkisId(),
                            step.getAdimSirasi()
                    );

            if (next.isEmpty()) {

                surec.setDurum("TAMAMLANDI");
                surec.setBitisTarihi(LocalDateTime.now());
                surecRepository.save(surec);

                // 🔥 PDF
                pdfReportService.generate(surec.getSurecId());

                workflowEngineService.resumeParentIfNeeded(surec, taskService);
                return null;
            }

            return next.get().getAdimId();
        }

        return null;
    }
}