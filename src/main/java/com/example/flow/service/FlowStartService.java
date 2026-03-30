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
    private final FormRepository formRepository; // ✅ EKLENDİ
    private final FormBileseniRepository formBilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;
    private final SurecRepository surecRepository;
    private final SurecAdimRepository surecAdimRepository;

    @Transactional
    public FlowStartResponse startFlow(Long akisId, Long userId) {

        // 1️⃣ ilk step
        AkisAdim firstStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(akisId)
                .orElseThrow();

        // 2️⃣ süreç oluştur
        AkisSurec surec = new AkisSurec();
        surec.setAkisId(akisId);
        surec.setBaslatanKullaniciId(userId);
        surec.setMevcutAdimId(firstStep.getAdimId());
        surec.setDurum("DEVAM");
        surec.setBaslamaTarihi(LocalDateTime.now());

        surecRepository.save(surec);

        // 🔥 3️⃣ FORM BUL (KRİTİK)
        Form form = formRepository.findByAdimId(firstStep.getAdimId())
                .orElseThrow(() -> new RuntimeException("Form bulunamadı"));

        // 🔥 4️⃣ BİLEŞENLERİ AL
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

        // 🔥 6️⃣ TASK OLUŞTUR
        for (Long kId : kullanicilar) {

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