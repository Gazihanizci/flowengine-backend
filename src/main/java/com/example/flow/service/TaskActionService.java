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
    private final FormVeriRepository formVeriRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FieldPermissionService fieldPermissionService;

    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;

    @Transactional
    public void handleAction(Long taskId,
                             Long aksiyonId,
                             Map<Long, String> formData) {

        // 1️⃣ TASK BUL
        SurecAdim task = surecAdimRepository.findById(taskId)
                .orElseThrow();

        Long userId = task.getAtananKullaniciId();

        // 2️⃣ FORM VERİLERİNİ KAYDET (🔥 YETKİ KONTROLLÜ)
        for (Map.Entry<Long, String> entry : formData.entrySet()) {

            // 🔥 YETKİ KONTROLÜ
            fieldPermissionService.validate(userId, entry.getKey());

            FormVeri fv = new FormVeri();
            fv.setSurecId(task.getSurecId());
            fv.setBilesenId(entry.getKey());
            fv.setDeger(entry.getValue());
            fv.setKaydedenKullaniciId(userId);
            fv.setKayitTarihi(LocalDateTime.now());

            formVeriRepository.save(fv);
        }

        // 3️⃣ TASK TAMAMLA
        task.setDurum("TAMAMLANDI");
        task.setTamamlandiMi(true);

        // 🔥 PARALLEL TASKLARI İPTAL
        cancelOtherTasks(task);

        // 4️⃣ SÜREÇ BUL
        AkisSurec surec = surecRepository
                .findById(task.getSurecId())
                .orElseThrow();

        Long nextStepId = null;

        // 5️⃣ DB KURAL VAR MI?
        List<AdimGecisKural> rules =
                gecisRepository.findByAdimIdAndAksiyonId(
                        task.getAdimId(),
                        aksiyonId
                );

        if (!rules.isEmpty()) {
            nextStepId = rules.get(0).getSonrakiAdimId();
        }

        // 6️⃣ DEFAULT LOGIC (DB yoksa)
        if (nextStepId == null) {

            AkisAdim currentStep = akisAdimRepository
                    .findById(task.getAdimId())
                    .orElseThrow();

            Integer sirasi = currentStep.getAdimSirasi();

            // ✅ ONAY → sonraki step
            if (aksiyonId == 1) {

                AkisAdim nextStep = akisAdimRepository
                        .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                                surec.getAkisId(),
                                sirasi
                        )
                        .orElse(null);

                if (nextStep == null) {
                    surec.setDurum("TAMAMLANDI");
                    return;
                }

                nextStepId = nextStep.getAdimId();
            }

            // ❌ RED → süreci bitir
            else if (aksiyonId == 2) {
                surec.setDurum("REDDEDILDI");
                return;
            }
        }

        // 7️⃣ SÜRECİ GÜNCELLE
        surec.setMevcutAdimId(nextStepId);

        // 8️⃣ YENİ TASK OLUŞTUR
        createTasksForStep(surec.getSurecId(), nextStepId);
    }

    // 🔥 PARALLEL TASK İPTAL
    private void cancelOtherTasks(SurecAdim currentTask) {

        List<SurecAdim> tasks =
                surecAdimRepository
                        .findBySurecIdAndAdimId(
                                currentTask.getSurecId(),
                                currentTask.getAdimId()
                        );

        for (SurecAdim t : tasks) {
            if (!t.getId().equals(currentTask.getId())
                    && "BEKLIYOR".equals(t.getDurum())) {

                t.setDurum("IPTAL");
                t.setTamamlandiMi(false);
            }
        }
    }

    // 🔥 YENİ STEP TASK OLUŞTUR
    private void createTasksForStep(Long surecId, Long adimId) {

        Form form = formRepository.findByAdimId(adimId)
                .orElseThrow();

        List<FormBileseni> bilesenler =
                formBilesenRepository.findByForm_FormId(form.getFormId());

        Set<Long> kullanicilar = new HashSet<>();

        for (FormBileseni b : bilesenler) {

            List<FormBileseniAtama> atamalar =
                    atamaRepository.findByBilesenId(b.getBilesenId());

            for (FormBileseniAtama a : atamalar) {

                if ("USER".equals(a.getTip())) {
                    kullanicilar.add(a.getRefId());
                }

                if ("ROLE".equals(a.getTip())) {

                    List<KullaniciRol> roller =
                            kullaniciRolRepository.findByRolId(a.getRefId());

                    for (KullaniciRol kr : roller) {
                        kullanicilar.add(kr.getKullaniciId());
                    }
                }
            }
        }

        for (Long kId : kullanicilar) {

            SurecAdim task = new SurecAdim();
            task.setSurecId(surecId);
            task.setAdimId(adimId);
            task.setAtananKullaniciId(kId);
            task.setDurum("BEKLIYOR");
            task.setTamamlandiMi(false);
            task.setBaslamaTarihi(LocalDateTime.now());

            surecAdimRepository.save(task);
        }
    }
}