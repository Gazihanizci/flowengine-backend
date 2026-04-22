package com.example.flow.service;

import com.example.flow.dto.UserHistoryResponse;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.example.flow.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHistoryService {

    private final SurecHareketRepository hareketRepository;
    private final AkisRepository akisRepository;
    private final SurecRepository surecRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final FormVeriRepository formVeriRepository;
    private final FormBileseniRepository bilesenRepository;
    private final CurrentUser currentUser;
    public List<UserHistoryResponse> getMyHistory() {

        // 🔥 JWT'den kullanıcı al
        Long userId = currentUser.id(); // ✅ DOĞRU KULLANIM

        List<SurecHareket> hareketler =
                hareketRepository.findByYapanKullaniciIdOrderByTarihDesc(userId);

        List<UserHistoryResponse> list = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (SurecHareket h : hareketler) {

            // 🔥 SÜREÇ → AKIŞ
            AkisSurec surec = surecRepository
                    .findById(h.getSurecId())
                    .orElse(null);

            String akisAdi = "Bilinmiyor";

            if (surec != null) {
                Akis akis = akisRepository
                        .findById(surec.getAkisId())
                        .orElse(null);

                if (akis != null) {
                    akisAdi = akis.getAkisAdi();
                }
            }

            // 🔥 ADIM ADI
            String adimAdi = akisAdimRepository
                    .findById(h.getAdimId())
                    .map(AkisAdim::getAdimAdi)
                    .orElse("Adım");

            // 🔥 AKSİYON
            String aksiyon = getAksiyonAdi(h.getAksiyonId());

            // 🔥 FORM VERİLERİ
            List<FormVeri> veriler =
                    formVeriRepository.findBySurecId(h.getSurecId());

            StringBuilder formText = new StringBuilder();

            for (FormVeri fv : veriler) {

                FormBileseni b = bilesenRepository
                        .findById(fv.getBilesenId())
                        .orElse(null);

                if (b != null) {
                    formText.append(b.getLabel())
                            .append(": ")
                            .append(fv.getDeger())
                            .append(" | ");
                }
            }

            list.add(
                    UserHistoryResponse.builder()
                            .surecId(h.getSurecId())
                            .akisAdi(akisAdi)
                            .adimAdi(adimAdi)
                            .aksiyon(aksiyon)
                            .formIcerik(formText.toString())
                            .tarih(h.getTarih().format(formatter))
                            .aciklama(h.getAciklama()) // 💥 EKLENDİ
                            .build()
            );
        }

        return list;
    }

    private String getAksiyonAdi(Long aksiyonId) {

        if (aksiyonId == null) return "Bilinmiyor";

        switch (aksiyonId.intValue()) {
            case 1: return "ONAYLANDI";
            case 2: return "KAYDEDİLDİ";
            case 3: return "REDDEDİLDİ";
            default: return "Bilinmeyen";
        }
    }
}