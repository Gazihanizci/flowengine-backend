package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    @Transactional
    public void handleAction(Long taskId, Long aksiyonId, Map<Long, String> formData) {
        if (aksiyonId == null || (aksiyonId != 1 && aksiyonId != 2)) {
            throw new RuntimeException("Geçersiz aksiyon");
        }

        SurecAdim currentTask = surecAdimRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task bulunamadı: " + taskId));

        // 1. Form Doğrulama ve Kaydetme
        formService.validateAndSaveFormData(currentTask.getSurecId(), currentTask.getAdimId(),
                currentTask.getAtananKullaniciId(), formData);

        // 2. Mevcut Taskı Kapat
        currentTask.setDurum("TAMAMLANDI");
        currentTask.setTamamlandiMi(true);
        currentTask.setBitisTarihi(LocalDateTime.now());
        surecAdimRepository.save(currentTask);

        // 3. Adım bazında tüm tasklar bitti mi kontrolü
        if (!taskService.isStepFullyCompleted(currentTask.getSurecId(), currentTask.getAdimId())) {
            return;
        }

        AkisAdim step = akisAdimRepository.findById(currentTask.getAdimId())
                .orElseThrow(() -> new RuntimeException("Adım bulunamadı"));

        AkisSurec surec = surecRepository.findById(currentTask.getSurecId())
                .orElseThrow(() -> new RuntimeException("Süreç bulunamadı"));

        // 4. DIŞ AKIŞ KONTROLÜ (Adım 1 tetikleyici ise alt flow başlar)
        if (Boolean.TRUE.equals(step.getExternalFlowEnabled()) && step.getExternalFlowId() != null) {
            workflowEngineService.startExternalFlow(surec, step, taskService);
            return;
        }

        // 5. SONRAKİ ADIM BELİRLEME
        Long nextStepId = determineNextStepId(surec, step, aksiyonId);

        if (nextStepId != null) {
            surec.setMevcutAdimId(nextStepId);
            surecRepository.save(surec);
            taskService.createTasksForStep(surec.getSurecId(), nextStepId);
        }
    }

    private Long determineNextStepId(AkisSurec surec, AkisAdim step, Long aksiyonId) {
        List<AdimGecisKural> rules = gecisRepository.findByAdimIdAndAksiyonId(step.getAdimId(), aksiyonId);

        if (!rules.isEmpty()) {
            return rules.get(0).getSonrakiAdimId();
        }

        if (aksiyonId == 1) { // Onay
            Optional<AkisAdim> next = akisAdimRepository
                    .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                            surec.getAkisId(), step.getAdimSirasi());

            if (next.isEmpty()) {
                surec.setDurum("TAMAMLANDI");
                surec.setBitisTarihi(LocalDateTime.now());
                surecRepository.save(surec);

                // 🔥 KRİTİK: Akış bittiğinde eğer bu bir child ise babayı uyandırıyoruz
                workflowEngineService.resumeParentIfNeeded(surec, taskService);
                return null;
            }
            return next.get().getAdimId();

        } else if (aksiyonId == 2) { // Red
            surec.setDurum("REDDEDILDI");
            surec.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(surec);
            workflowEngineService.handleChildRejected(surec);
            return null;
        }
        return null;
    }
}