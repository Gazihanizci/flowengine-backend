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
    private final SurecEventiRepository surecEventiRepository;

    @Transactional
    public void handleAction(Long taskId, Long aksiyonId, Map<Long, String> formData) {

        if (aksiyonId == null || (aksiyonId != 1 && aksiyonId != 2)) {
            throw new RuntimeException("Geçersiz aksiyon");
        }

        SurecAdim currentTask = surecAdimRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task bulunamadı: " + taskId));

        Long userId = currentTask.getAtananKullaniciId();

        Form form = formRepository
                .findByAdimId(currentTask.getAdimId())
                .orElse(null);

        if (form != null) {
            List<FormBileseni> bilesenler =
                    formBilesenRepository.findByForm_FormId(form.getFormId());

            for (FormBileseni b : bilesenler) {
                if (Boolean.TRUE.equals(b.getZorunlu())) {
                    if (formData == null || !formData.containsKey(b.getBilesenId())) {
                        throw new RuntimeException("Zorunlu alan boş: " + b.getLabel());
                    }
                }
            }
        }

        if (formData != null) {
            for (Map.Entry<Long, String> entry : formData.entrySet()) {

                fieldPermissionService.validate(userId, entry.getKey());

                Optional<FormVeri> existing =
                        formVeriRepository.findBySurecIdAndBilesenId(
                                currentTask.getSurecId(),
                                entry.getKey()
                        );

                FormVeri fv;
                if (existing.isPresent()) {
                    fv = existing.get();
                    fv.setDeger(entry.getValue());
                } else {
                    fv = new FormVeri();
                    fv.setSurecId(currentTask.getSurecId());
                    fv.setBilesenId(entry.getKey());
                    fv.setDeger(entry.getValue());
                }

                fv.setKaydedenKullaniciId(userId);
                fv.setKayitTarihi(LocalDateTime.now());
                formVeriRepository.save(fv);
            }
        }

        currentTask.setDurum("TAMAMLANDI");
        currentTask.setTamamlandiMi(true);
        currentTask.setBitisTarihi(LocalDateTime.now());
        surecAdimRepository.save(currentTask);

        long tamamlanan = surecAdimRepository.countBySurecIdAndAdimIdAndDurum(
                currentTask.getSurecId(),
                currentTask.getAdimId(),
                "TAMAMLANDI"
        );

        long toplam = surecAdimRepository.findBySurecIdAndAdimId(
                currentTask.getSurecId(),
                currentTask.getAdimId()
        ).size();

        if (tamamlanan < toplam) {
            return;
        }

        AkisAdim step = akisAdimRepository.findById(currentTask.getAdimId())
                .orElseThrow(() -> new RuntimeException("Adım bulunamadı"));

        AkisSurec surec = surecRepository.findById(currentTask.getSurecId())
                .orElseThrow(() -> new RuntimeException("Süreç bulunamadı"));

        // DIŞ AKIŞ BAŞLATMA
        if (Boolean.TRUE.equals(step.getExternalFlowEnabled())
                && step.getExternalFlowId() != null) {

            surec.setDurum("WAITING_EXTERNAL");
            surec.setMevcutAdimId(step.getAdimId());
            surecRepository.save(surec);

            AkisSurec child = new AkisSurec();
            child.setAkisId(step.getExternalFlowId());
            child.setBaslatanKullaniciId(surec.getBaslatanKullaniciId());
            child.setMevcutAdimId(null);
            child.setDurum("RUNNING");
            child.setBaslamaTarihi(LocalDateTime.now());
            child.setParentSurecId(surec.getSurecId());
            child.setParentAdimId(step.getAdimId());
            child.setResumeAdimSirasi(step.getAdimSirasi() + 1);

            surecRepository.save(child);

            AkisAdim firstChildStep = akisAdimRepository
                    .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(step.getExternalFlowId())
                    .orElseThrow(() -> new RuntimeException("Child flow ilk adımı bulunamadı"));

            child.setMevcutAdimId(firstChildStep.getAdimId());
            surecRepository.save(child);

            createTasksForStep(child.getSurecId(), firstChildStep.getAdimId());
            return;
        }

        Long nextStepId = null;

        List<AdimGecisKural> rules =
                gecisRepository.findByAdimIdAndAksiyonId(
                        currentTask.getAdimId(),
                        aksiyonId
                );

        if (!rules.isEmpty()) {
            nextStepId = rules.get(0).getSonrakiAdimId();
        }

        if (nextStepId == null) {
            if (aksiyonId == 1) {
                Optional<AkisAdim> next =
                        akisAdimRepository
                                .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                                        surec.getAkisId(),
                                        step.getAdimSirasi()
                                );

                if (next.isEmpty()) {
                    surec.setDurum("TAMAMLANDI");
                    surec.setBitisTarihi(LocalDateTime.now());
                    surecRepository.save(surec);

                    // child ise parent'ı resume et
                    resumeParentIfNeeded(surec);
                    return;
                }

                nextStepId = next.get().getAdimId();
            } else if (aksiyonId == 2) {
                surec.setDurum("REDDEDILDI");
                surec.setBitisTarihi(LocalDateTime.now());
                surecRepository.save(surec);

                // child red olursa parent davranışı
                handleChildRejectedIfNeeded(surec);
                return;
            }
        }

        if (nextStepId != null) {
            surec.setMevcutAdimId(nextStepId);
            surecRepository.save(surec);
            createTasksForStep(surec.getSurecId(), nextStepId);
        }
    }

    @Transactional
    public void resumeParentIfNeeded(AkisSurec childSurec) {

        if (childSurec.getParentSurecId() == null) {
            return;
        }

        String correlationId = "CHILD_COMPLETED_" + childSurec.getSurecId();

        if (surecEventiRepository.existsByCorrelationId(correlationId)) {
            return;
        }

        SurecEventi event = new SurecEventi();
        event.setSurecId(childSurec.getSurecId());
        event.setEventType("CHILD_COMPLETED");
        event.setCorrelationId(correlationId);
        event.setProcessedAt(LocalDateTime.now());
        surecEventiRepository.save(event);

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        parent.setDurum("RUNNING");

        Optional<AkisAdim> next = akisAdimRepository
                .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                        parent.getAkisId(),
                        childSurec.getResumeAdimSirasi() - 1
                );

        if (next.isEmpty()) {
            parent.setDurum("TAMAMLANDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
            return;
        }

        parent.setMevcutAdimId(next.get().getAdimId());
        surecRepository.save(parent);

        createTasksForStep(parent.getSurecId(), next.get().getAdimId());
    }

    @Transactional
    public void handleChildRejectedIfNeeded(AkisSurec childSurec) {

        if (childSurec.getParentSurecId() == null) {
            return;
        }

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        AkisAdim parentStep = akisAdimRepository.findById(childSurec.getParentAdimId())
                .orElseThrow(() -> new RuntimeException("Parent step bulunamadı"));

        String davranis = parentStep.getCancelBehavior();

        if (davranis == null || "PROPAGATE".equalsIgnoreCase(davranis)) {
            parent.setDurum("REDDEDILDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
        } else if ("KEEP_WAITING".equalsIgnoreCase(davranis)) {
            parent.setDurum("WAITING_EXTERNAL");
            surecRepository.save(parent);
        }
    }

    private void createTasksForStep(Long surecId, Long adimId) {

        Form form = formRepository.findByAdimId(adimId)
                .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

        List<FormBileseni> bilesenler =
                formBilesenRepository.findByForm_FormId(form.getFormId());

        Set<Long> users = new HashSet<>();

        for (FormBileseni b : bilesenler) {
            List<FormBileseniAtama> atamalar =
                    atamaRepository.findByBilesenId(b.getBilesenId());

            for (FormBileseniAtama a : atamalar) {
                if ("USER".equals(a.getTip())) {
                    users.add(a.getRefId());
                } else if ("ROLE".equals(a.getTip())) {
                    List<KullaniciRol> roller =
                            kullaniciRolRepository.findByRolId(a.getRefId());

                    for (KullaniciRol kr : roller) {
                        users.add(kr.getKullaniciId());
                    }
                }
            }
        }

        if (users.isEmpty()) {
            throw new RuntimeException("Atanacak kullanıcı bulunamadı!");
        }

        for (Long uid : users) {
            SurecAdim task = new SurecAdim();
            task.setSurecId(surecId);
            task.setAdimId(adimId);
            task.setAtananKullaniciId(uid);
            task.setDurum("BEKLIYOR");
            task.setTamamlandiMi(false);
            task.setBaslamaTarihi(LocalDateTime.now());
            surecAdimRepository.save(task);
        }
    }
}