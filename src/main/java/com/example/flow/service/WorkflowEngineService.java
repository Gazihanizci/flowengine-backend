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
public class WorkflowEngineService {

    private final SurecRepository surecRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final SurecEventiRepository surecEventiRepository;

    // İzinler için gerekli repolar (Senin orijinalinde vardı)
    private final FlowBaslatmaYetkiRepository yetkiRepository;
    private final FlowBaslatmaIstekRepository istekRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final BildirimRepository bildirimRepository;

    @Transactional
    public void startExternalFlow(AkisSurec parentSurec, AkisAdim step, TaskService taskService) {
        Long userId = parentSurec.getBaslatanKullaniciId();

        // --- İZİN KONTROLÜ BAŞLANGIÇ ---
        boolean yetkiliMi = checkPermission(step.getExternalFlowId(), userId);

        if (!yetkiliMi) {
            // İzin İsteği Oluştur
            final FlowBaslatmaIstek istek = new FlowBaslatmaIstek();
            istek.setAkisId(step.getExternalFlowId());
            istek.setIsteyenKullaniciId(userId);
            istek.setDurum("BEKLIYOR");
            istek.setOlusturmaTarihi(LocalDateTime.now());

            // Geri dönüş bilgileri
            istek.setParentSurecId(parentSurec.getSurecId());
            istek.setParentAdimId(step.getAdimId());
            istek.setResumeAdimSirasi(step.getAdimSirasi() + 1);

            final FlowBaslatmaIstek savedIstek = istekRepository.save(istek);
            sendNotifications(step.getExternalFlowId(), savedIstek.getId());

            parentSurec.setDurum("WAITING_APPROVAL");
            surecRepository.save(parentSurec);
            return;
        }
        // --- İZİN KONTROLÜ BİTİŞ ---

        // ✅ YETKİ VARSA DOĞRUDAN BAŞLAT (Senin orijinal metodun)
        launchChild(parentSurec, step, taskService);
    }

    private void launchChild(AkisSurec parentSurec, AkisAdim step, TaskService taskService) {
        parentSurec.setDurum("WAITING_EXTERNAL");
        parentSurec.setMevcutAdimId(step.getAdimId());
        surecRepository.save(parentSurec);

        AkisSurec child = new AkisSurec();
        child.setAkisId(step.getExternalFlowId());
        child.setBaslatanKullaniciId(parentSurec.getBaslatanKullaniciId());
        child.setMevcutAdimId(null);
        child.setDurum("RUNNING");
        child.setBaslamaTarihi(LocalDateTime.now());
        child.setParentSurecId(parentSurec.getSurecId());
        child.setParentAdimId(step.getAdimId());
        child.setResumeAdimSirasi(step.getAdimSirasi() + 1);

        surecRepository.save(child);

        AkisAdim firstChildStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(step.getExternalFlowId())
                .orElseThrow(() -> new RuntimeException("Child flow ilk adımı bulunamadı"));

        child.setMevcutAdimId(firstChildStep.getAdimId());
        surecRepository.save(child);

        taskService.createTasksForStep(child.getSurecId(), firstChildStep.getAdimId());
    }

    @Transactional
    public void resumeParentIfNeeded(AkisSurec childSurec, TaskService taskService) {
        if (childSurec.getParentSurecId() == null) return;

        String correlationId = "ALT_TAMAMLANDI_" + childSurec.getSurecId();
        if (surecEventiRepository.existsByCorrelationId(correlationId)) return;

        SurecEventi event = new SurecEventi();
        event.setSurecId(childSurec.getSurecId());
        event.setEventType("ALT_TAMAMLANDI"); // Senin orijinalindeki alt çizgi düzeltildi
        event.setCorrelationId(correlationId);
        event.setProcessedAt(LocalDateTime.now());
        surecEventiRepository.save(event);

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        parent.setDurum("DEVAM_EDIYOR");

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
        taskService.createTasksForStep(parent.getSurecId(), next.get().getAdimId());
    }

    @Transactional
    public void handleChildRejected(AkisSurec childSurec) {
        if (childSurec.getParentSurecId() == null) return;

        AkisSurec parent = surecRepository.findById(childSurec.getParentSurecId())
                .orElseThrow(() -> new RuntimeException("Parent süreç bulunamadı"));

        AkisAdim parentStep = akisAdimRepository.findById(childSurec.getParentAdimId())
                .orElseThrow(() -> new RuntimeException("Parent step bulunamadı"));

        String davranis = parentStep.getCancelBehavior();

        if (davranis == null || "Red Yansıması".equalsIgnoreCase(davranis)) {
            parent.setDurum("REDDEDILDI");
            parent.setBitisTarihi(LocalDateTime.now());
            surecRepository.save(parent);
        } else if ("BEKLEMEYE_DEVAM".equalsIgnoreCase(davranis)) {
            parent.setDurum("HARICI_BEKLIYOR");
            surecRepository.save(parent);
        }
    }

    // --- Yardımcı Metotlar ---
    private boolean checkPermission(Long flowId, Long userId) {
        if (yetkiRepository.existsByAkisIdAndTipAndRefId(flowId, "USER", userId)) return true;
        List<KullaniciRol> roller = kullaniciRolRepository.findByKullaniciId(userId);
        for (KullaniciRol rol : roller) {
            if (yetkiRepository.existsByAkisIdAndTipAndRefId(flowId, "ROLE", rol.getRolId())) return true;
        }
        return false;
    }

    private void sendNotifications(Long flowId, Long requestId) {
        List<FlowBaslatmaYetki> yetkiler = yetkiRepository.findByAkisId(flowId);
        for (FlowBaslatmaYetki y : yetkiler) {
            if ("USER".equalsIgnoreCase(y.getTip())) {
                saveBildirim(y.getRefId(), requestId);
            } else if ("ROLE".equalsIgnoreCase(y.getTip())) {
                kullaniciRolRepository.findByRolId(y.getRefId())
                        .forEach(kr -> saveBildirim(kr.getKullaniciId(), requestId));
            }
        }
    }

    private void saveBildirim(Long userId, Long requestId) {
        Bildirim b = new Bildirim();
        b.setKullaniciId(userId);
        b.setBaslik("Alt Flow Başlatma İsteği");
        b.setMesaj("Alt flow için onay gerekiyor");
        b.setTip("SUBFLOW_REQUEST");
        b.setOkundu(false);
        b.setOlusturmaTarihi(LocalDateTime.now());
        b.setReferansIstekId(requestId);
        bildirimRepository.save(b);
    }
}