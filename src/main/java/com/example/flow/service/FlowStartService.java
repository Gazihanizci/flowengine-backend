package com.example.flow.service;

import com.example.flow.dto.FlowStartResponse;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FlowStartService {

    private final AkisAdimRepository akisAdimRepository;
    private final FormRepository formRepository;
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final SurecRepository surecRepository;
    private final SurecAdimRepository surecAdimRepository;
    private final BildirimRepository bildirimRepository;

    private final FlowBaslatmaYetkiRepository yetkiRepository;
    private final FlowBaslatmaIstekRepository istekRepository;

    private final KullaniciRepository kullaniciRepository;
    private final AkisRepository akisRepository;

    public FlowStartResponse startFlow(Long akisId, Long userId) {
        return startFlow(akisId, userId, false, null);
    }

    public FlowStartResponse startFlow(Long akisId, Long userId, boolean forceStart) {
        return startFlow(akisId, userId, forceStart, null);
    }

    @Transactional
    public FlowStartResponse startFlow(
            Long akisId,
            Long userId,
            boolean forceStart,
            Set<Long> assignedUserIds
    ) {

        if (userId == null || userId <= 0) {
            throw new RuntimeException("Geçersiz kullanıcı ID");
        }

        Kullanici kullanici = kullaniciRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Akis akis = akisRepository.findById(akisId)
                .orElseThrow(() -> new RuntimeException("Akış bulunamadı"));

        String kullaniciAdi = kullanici.getAdSoyad();
        String akisAdi = akis.getAkisAdi();

        // 🔥 HER ZAMAN İSTEK OLUŞTUR (forceStart değilse)
        if (!forceStart) {

            FlowBaslatmaIstek istek = new FlowBaslatmaIstek();
            istek.setAkisId(akisId);
            istek.setIsteyenKullaniciId(userId);
            istek.setDurum("BEKLIYOR");
            istek.setOlusturmaTarihi(LocalDateTime.now());

            // 🔥 EKLENDİ (assigned user taşıma)
            istek.setAssignedUserIds(assignedUserIds);

            istek = istekRepository.save(istek);

            List<FlowBaslatmaYetki> yetkiler =
                    yetkiRepository.findByAkisId(akisId);

            Set<Long> hedefKullanicilar = new HashSet<>();

            for (FlowBaslatmaYetki y : yetkiler) {

                if ("USER".equalsIgnoreCase(y.getTip())) {
                    hedefKullanicilar.add(y.getRefId());
                }

                if ("ROLE".equalsIgnoreCase(y.getTip())) {

                    List<KullaniciRol> roller =
                            kullaniciRolRepository.findByRolId(y.getRefId());

                    for (KullaniciRol kr : roller) {
                        hedefKullanicilar.add(kr.getKullaniciId());
                    }
                }
            }

            for (Long kId : hedefKullanicilar) {

                Bildirim b = new Bildirim();
                b.setKullaniciId(kId);

                b.setBaslik("Flow Başlatma İsteği");
                b.setMesaj(kullaniciAdi + " → '" + akisAdi + "' akışını başlatmak istiyor");

                b.setTip("FLOW_REQUEST");
                b.setOkundu(false);
                b.setOlusturmaTarihi(LocalDateTime.now());

                b.setReferansIstekId(istek.getId());
                b.setGonderenKullaniciId(userId);
                b.setAkisId(akisId);

                bildirimRepository.save(b);
            }

            return new FlowStartResponse(null, null, "Başlatma isteği gönderildi");
        }

        // 🔥 FLOW BAŞLAT (APPROVE SONRASI)

        AkisAdim firstStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(akisId)
                .orElseThrow(() -> new RuntimeException("İlk adım bulunamadı"));

        AkisSurec surec = new AkisSurec();

        surec.setAkisId(akisId);

        // 🔥🔥🔥 KRİTİK FIX (SORUNU ÇÖZEN SATIR)
        surec.setBaslatanKullaniciId(userId);

        surec.setMevcutAdimId(firstStep.getAdimId());
        surec.setDurum("DEVAM");
        surec.setBaslamaTarihi(LocalDateTime.now());

        surec = surecRepository.save(surec);

        Set<Long> atanacakKullanicilar = new HashSet<>();

        if (assignedUserIds != null && !assignedUserIds.isEmpty()) {

            atanacakKullanicilar.addAll(assignedUserIds);

        } else {

            Form form = formRepository.findByAdimId(firstStep.getAdimId())
                    .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

            List<FormBileseni> bilesenler =
                    formBilesenRepository.findByForm_FormId(form.getFormId());

            for (FormBileseni bilesen : bilesenler) {

                List<FormBileseniAtama> atamalar =
                        atamaRepository.findByBilesenId(bilesen.getBilesenId());

                for (FormBileseniAtama atama : atamalar) {

                    if ("USER".equalsIgnoreCase(atama.getTip())) {
                        atanacakKullanicilar.add(atama.getRefId());
                    }

                    if ("ROLE".equalsIgnoreCase(atama.getTip())) {

                        List<KullaniciRol> roller =
                                kullaniciRolRepository.findByRolId(atama.getRefId());

                        for (KullaniciRol kr : roller) {
                            atanacakKullanicilar.add(kr.getKullaniciId());
                        }
                    }
                }
            }
        }

        for (Long kId : atanacakKullanicilar) {

            SurecAdim task = new SurecAdim();
            task.setSurecId(surec.getSurecId());
            task.setAdimId(firstStep.getAdimId());
            task.setAtananKullaniciId(kId);
            task.setDurum("BEKLIYOR");
            task.setTamamlandiMi(false);
            task.setBaslamaTarihi(LocalDateTime.now());

            surecAdimRepository.save(task);
        }

        return new FlowStartResponse(
                surec.getSurecId(),
                firstStep.getAdimId(),
                "Flow başlatıldı"
        );
    }
}