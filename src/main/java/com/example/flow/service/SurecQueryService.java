package com.example.flow.service;

import com.example.flow.dto.SurecListResponse;
import com.example.flow.entity.Akis;
import com.example.flow.entity.AkisSurec;
import com.example.flow.repository.AkisRepository;
import com.example.flow.repository.SurecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurecQueryService {

    private final SurecRepository surecRepository;
    private final AkisRepository akisRepository;

    public List<SurecListResponse> getAll() {

        List<AkisSurec> surecler = surecRepository.findAll();

        List<SurecListResponse> list = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (AkisSurec s : surecler) {

            Akis akis = akisRepository
                    .findById(s.getAkisId())
                    .orElse(null);

            list.add(
                    SurecListResponse.builder()
                            .surecId(s.getSurecId())
                            .akisAdi(akis != null ? akis.getAkisAdi() : "Bilinmiyor")
                            .akisAciklama(akis != null ? akis.getAciklama() : "")
                            // 🔥 TARİH EKLENDİ
                            .baslamaTarihi(
                                    s.getBaslamaTarihi() != null
                                            ? s.getBaslamaTarihi().format(formatter)
                                            : "-"
                            )
                            .build()
            );
        }

        return list;
    }
}