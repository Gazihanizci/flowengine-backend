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

@RequiredArgsConstructor
@Service
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

    public FlowStartResponse startFlow(Long akisId, Long userId) {
        return startFlow(akisId, userId, false);
    }

    @Transactional
    public FlowStartResponse startFlow(Long akisId, Long userId, boolean forceStart) {

        boolean yetkiliMi = false;

        if (userId != null &&
                yetkiRepository.existsByAkisIdAndTipAndRefId(akisId, "USER", userId)) {
            yetkiliMi = true;
        }

        if (!yetkiliMi && userId != null) {
            List<KullaniciRol> roller =
                    kullaniciRolRepository.findByKullaniciId(userId);

            for (KullaniciRol rol : roller) {
                if (yetkiRepository.existsByAkisIdAndTipAndRefId(
                        akisId,
                        "ROLE",
                        rol.getRolId()
                )) {
                    yetkiliMi = true;
                    break;
                }
            }
        }

        // ❌ YETKİ YOK → İSTEK
        if (!yetkiliMi && !forceStart) {

            FlowBaslatmaIstek istek = new FlowBaslatmaIstek();
            istek.setAkisId(akisId);
            istek.setIsteyenKullaniciId(userId);
            istek.setDurum("BEKLIYOR");
            istek.setOlusturmaTarihi(LocalDateTime.now());

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
                b.setMesaj("Bir kullanıcı flow başlatmak istiyor");
                b.setTip("FLOW_REQUEST");
                b.setOkundu(false);
                b.setOlusturmaTarihi(LocalDateTime.now());
                b.setReferansIstekId(istek.getId());

                bildirimRepository.save(b);
            }

            return new FlowStartResponse(null, null, "Başlatma isteği gönderildi");
        }

        // 🔥 FLOW BAŞLATMA

        AkisAdim firstStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(akisId)
                .orElseThrow(() -> new RuntimeException("İlk adım bulunamadı"));

        AkisSurec surec = new AkisSurec();
        surec.setAkisId(akisId);
        // ❌ BURASI KALDIRILDI → FK HATASI BİTTİ
        // surec.setBaslatanKullaniciId(userId);

        surec.setMevcutAdimId(firstStep.getAdimId());
        surec.setDurum("DEVAM");
        surec.setBaslamaTarihi(LocalDateTime.now());

        surec = surecRepository.save(surec);

        Form form = formRepository.findByAdimId(firstStep.getAdimId())
                .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

        List<FormBileseni> bilesenler =
                formBilesenRepository.findByForm_FormId(form.getFormId());

        Set<Long> atanacakKullanicilar = new HashSet<>();

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

        if (atanacakKullanicilar.isEmpty()) {
            throw new RuntimeException("Bu adım için atanacak kullanıcı bulunamadı");
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