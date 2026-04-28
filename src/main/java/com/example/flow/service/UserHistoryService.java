package com.example.flow.service;

import com.example.flow.dto.UserHistoryResponse;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.example.flow.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Transactional(readOnly = true)
    public List<UserHistoryResponse> getMyHistory() {

        Long userId = currentUser.id();

        List<SurecHareket> hareketler =
                hareketRepository.findByYapanKullaniciIdOrderByTarihDesc(userId);

        List<UserHistoryResponse> result = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (SurecHareket h : hareketler) {

            // 🔥 AKIŞ ADI
            String akisAdi = surecRepository.findById(h.getSurecId())
                    .flatMap(s -> akisRepository.findById(s.getAkisId()))
                    .map(Akis::getAkisAdi)
                    .orElse("Bilinmiyor");

            // 🔥 ADIM ADI
            String adimAdi = akisAdimRepository
                    .findById(h.getAdimId())
                    .map(AkisAdim::getAdimAdi)
                    .orElse("Adım");

            // 🔥 AKSİYON
            String aksiyon = getAksiyonAdi(h.getAksiyonId());

            // 🔥 USER + SÜREÇ VERİLERİ
            List<FormVeri> veriler =
                    formVeriRepository.findBySurecIdAndKaydedenKullaniciId(
                            h.getSurecId(),
                            h.getYapanKullaniciId()
                    );

            // 🔥 duplicate field temizleme
            Map<Long, FormVeri> uniqueMap = new LinkedHashMap<>();
            for (FormVeri fv : veriler) {
                uniqueMap.put(fv.getBilesenId(), fv);
            }

            StringBuilder formText = new StringBuilder();

            for (FormVeri fv : uniqueMap.values()) {

                FormBileseni b = bilesenRepository
                        .findById(fv.getBilesenId())
                        .orElse(null);

                if (b == null) continue;

                // 🔥 LAZY SAFE + ADIM FİLTRESİ
                if (b.getForm() == null) continue;
                if (b.getForm().getAdim() == null) continue;
                if (b.getForm().getAdim().getAdimId() == null) continue;

                if (!b.getForm().getAdim().getAdimId().equals(h.getAdimId())) continue;

                formText.append(b.getLabel())
                        .append(": ")
                        .append(fv.getDeger());
            }

            result.add(
                    UserHistoryResponse.builder()
                            .surecId(h.getSurecId())
                            .akisAdi(akisAdi)
                            .adimAdi(adimAdi)
                            .aksiyon(aksiyon)
                            .formIcerik(formText.toString())
                            .tarih(h.getTarih().format(formatter))
                            .aciklama(h.getAciklama())
                            .build()
            );
        }

        return result;
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