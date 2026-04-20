package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.example.flow.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final SurecRepository surecRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final SurecEventiRepository surecEventiRepository;

    private final FlowBaslatmaYetkiRepository yetkiRepository;
    private final FlowBaslatmaIstekRepository istekRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final BildirimRepository bildirimRepository;

    private final KullaniciRepository kullaniciRepository;
    private final AkisRepository akisRepository;

    private final CurrentUser currentUser;

    @Transactional
    public void startExternalFlow(AkisSurec parentSurec, AkisAdim step, TaskService taskService) {

        Long isteyenUserId = currentUser.id();

        if (isteyenUserId == null || isteyenUserId <= 0) {
            throw new RuntimeException("Geçerli kullanıcı bulunamadı");
        }

        if (step.getExternalFlowId() == null) {
            throw new RuntimeException("externalFlowId yok");
        }

        // 🔥 parent freeze
        parentSurec.setDurum("WAITING_APPROVAL");
        parentSurec.setMevcutAdimId(step.getAdimId());
        surecRepository.save(parentSurec);

        // 🔥 izin isteği
        FlowBaslatmaIstek istek = new FlowBaslatmaIstek();
        istek.setAkisId(step.getExternalFlowId());
        istek.setIsteyenKullaniciId(isteyenUserId);
        istek.setDurum("BEKLIYOR");
        istek.setOlusturmaTarihi(LocalDateTime.now());

        istek.setParentSurecId(parentSurec.getSurecId());
        istek.setParentAdimId(step.getAdimId());
        istek.setResumeAdimSirasi(step.getAdimSirasi() + 1);

        FlowBaslatmaIstek saved = istekRepository.save(istek);

        int count = sendNotifications(step.getExternalFlowId(), saved.getId(), isteyenUserId);

        if (count == 0) {
            throw new RuntimeException("Bildirim gidecek kullanıcı yok");
        }
    }

    @Transactional
    public void resumeParentIfNeeded(AkisSurec childSurec, TaskService taskService) {

        if (childSurec.getParentSurecId() == null) return;

        // 🔥 duplicate çalışmayı engelle
        String key = "RESUME_" + childSurec.getSurecId();
        if (surecEventiRepository.existsByCorrelationId(key)) return;

        SurecEventi event = new SurecEventi();
        event.setSurecId(childSurec.getSurecId());
        event.setEventType("RESUME_PARENT");
        event.setCorrelationId(key);
        event.setProcessedAt(LocalDateTime.now());
        surecEventiRepository.save(event);

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent bulunamadı"));

        // 🔥 DEVAM
        parent.setDurum("DEVAM");

        AkisAdim parentStep = akisAdimRepository.findById(childSurec.getParentAdimId())
                .orElseThrow(() -> new RuntimeException("Parent step yok"));

        Optional<AkisAdim> nextStep = akisAdimRepository
                .findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
                        parent.getAkisId(),
                        parentStep.getAdimSirasi()
                );

        if (nextStep.isEmpty()) {
            parent.setDurum("TAMAMLANDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
            return;
        }

        // 🔥 EN KRİTİK DÜZELTME
        parent.setMevcutAdimId(nextStep.get().getAdimId());
        surecRepository.save(parent);

        // 🔥 TASK OLUŞTUR
        taskService.createTasksForStep(
                parent.getSurecId(),
                nextStep.get().getAdimId()
        );
    }

    @Transactional
    public void handleChildRejected(AkisSurec childSurec) {

        if (childSurec.getParentSurecId() == null) return;

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow();

        AkisAdim parentStep = akisAdimRepository.findById(childSurec.getParentAdimId())
                .orElseThrow();

        String davranis = parentStep.getCancelBehavior();

        if (davranis == null || "Red Yansıması".equalsIgnoreCase(davranis)) {
            parent.setDurum("REDDEDILDI");
            parent.setBitisTarihi(LocalDateTime.now());
        } else {
            parent.setDurum("HARICI_BEKLIYOR");
        }

        surecRepository.save(parent);
    }

    private int sendNotifications(Long flowId, Long requestId, Long isteyenUserId) {

        List<FlowBaslatmaYetki> yetkiler = yetkiRepository.findByAkisId(flowId);
        Set<Long> users = new HashSet<>();

        for (FlowBaslatmaYetki y : yetkiler) {

            if ("USER".equalsIgnoreCase(y.getTip())) {
                users.add(y.getRefId());
            }

            if ("ROLE".equalsIgnoreCase(y.getTip())) {
                kullaniciRolRepository.findByRolId(y.getRefId())
                        .forEach(r -> users.add(r.getKullaniciId()));
            }
        }

        for (Long u : users) {
            saveBildirim(u, requestId, isteyenUserId, flowId);
        }

        return users.size();
    }

    private void saveBildirim(Long userId, Long requestId, Long isteyenUserId, Long akisId) {

        String ad = kullaniciRepository.findById(isteyenUserId)
                .map(Kullanici::getAdSoyad)
                .orElse("Bilinmeyen");

        String akis = akisRepository.findById(akisId)
                .map(Akis::getAkisAdi)
                .orElse("Akış");

        Bildirim b = new Bildirim();
        b.setKullaniciId(userId);
        b.setBaslik("Alt Flow Başlatma");
        b.setMesaj(ad + " → " + akis);
        b.setTip("SUBFLOW_REQUEST");
        b.setOkundu(false);
        b.setOlusturmaTarihi(LocalDateTime.now());

        b.setReferansIstekId(requestId);
        b.setGonderenKullaniciId(isteyenUserId);
        b.setAkisId(akisId);

        bildirimRepository.save(b);
    }
}