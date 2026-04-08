package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowMapService {

    private final AkisRepository akisRepository;
    private final AkisAdimRepository adimRepository;
    private final AdimGecisKuralRepository gecisRepository;
    private final FormBileseniRepository bilesenRepository;
    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRepository kullaniciRepository;
    private final KullaniciRolRepository kullaniciRolRepository;

    /**
     * Controller'ın aradığı metot ismi tam olarak budur.
     */
    public FlowMapResponse getFullFlowStructure(Long parentAkisId) {
        Akis parentAkis = akisRepository.findById(parentAkisId)
                .orElseThrow(() -> new RuntimeException("Ana akış bulunamadı: " + parentAkisId));

        List<StepNodeDTO> fullTimeline = new ArrayList<>();

        // 1. Ana Akışın tüm adımlarını senin repodaki metoda göre çek (findByAkis_AkisIdOrderByAdimSirasi)
        List<AkisAdim> allParentSteps = adimRepository.findByAkis_AkisIdOrderByAdimSirasi(parentAkisId);

        // SQL örneğindeki gibi tetikleyiciyi bul (external_flow_enabled = 1)
        AkisAdim triggerStep = allParentSteps.stream()
                .filter(a -> Boolean.TRUE.equals(a.getExternalFlowEnabled()))
                .findFirst()
                .orElse(null);

        Integer triggerOrder = (triggerStep != null) ? triggerStep.getAdimSirasi() : Integer.MAX_VALUE;

        // SQL MANTIĞI UYGULANIYOR:
        for (AkisAdim adim : allParentSteps) {

            // --- KISIM 1: Tetikleyiciye kadar olan ve tetikleyici dahil ana adımlar ---
            if (adim.getAdimSirasi() <= triggerOrder) {
                fullTimeline.add(mapToDto(adim, "1-ANA"));
            }

            // --- KISIM 2: Eğer tetikleyici adımdaysak, araya Alt Akış adımlarını enjekte et ---
            if (triggerStep != null && adim.getAdimId().equals(triggerStep.getAdimId()) && adim.getExternalFlowId() != null) {
                List<AkisAdim> childSteps = adimRepository.findByAkis_AkisIdOrderByAdimSirasi(adim.getExternalFlowId());
                for (AkisAdim childAdim : childSteps) {
                    fullTimeline.add(mapToDto(childAdim, "2-CHILD (Subflow: " + adim.getExternalFlowId() + ")"));
                }
            }

            // --- KISIM 3: Alt akıştan sonraki ana akış adımları ---
            if (adim.getAdimSirasi() > triggerOrder) {
                fullTimeline.add(mapToDto(adim, "3-ANA-DEVAM"));
            }
        }

        return FlowMapResponse.builder()
                .akisId(parentAkisId)
                .akisAdi(parentAkis.getAkisAdi())
                .adimlar(fullTimeline)
                .build();
    }

    private StepNodeDTO mapToDto(AkisAdim adim, String evre) {
        return StepNodeDTO.builder()
                .evre(evre)
                .adimId(adim.getAdimId())
                .adimAdi(adim.getAdimAdi())
                .sira(adim.getAdimSirasi())
                .tip(Boolean.TRUE.equals(adim.getExternalFlowEnabled()) ? "TETIKLEYICI" : "-")
                .bilesenler(fetchComponents(adim.getAdimId()))
                .build();
    }

    private List<ComponentNodeDTO> fetchComponents(Long adimId) {
        return bilesenRepository.findByForm_FormId(adimId).stream()
                .map(b -> ComponentNodeDTO.builder()
                        .etiket(b.getLabel())
                        .tip(b.getBilesenTipi())
                        .yetkiliIsimleri(getAuthorizedNames(b.getBilesenId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> getAuthorizedNames(Long bilesenId) {
        List<String> names = new ArrayList<>();
        List<FormBileseniAtama> atamalar = atamaRepository.findByBilesenId(bilesenId);

        for (FormBileseniAtama atama : atamalar) {
            if ("USER".equalsIgnoreCase(atama.getTip())) {
                kullaniciRepository.findById(atama.getRefId())
                        .ifPresent(u -> names.add(u.getAdSoyad()));
            } else if ("ROLE".equalsIgnoreCase(atama.getTip())) {
                // Senin paylaştığın findByRolId metodunu kullanıyoruz
                List<KullaniciRol> roller = kullaniciRolRepository.findByRolId(atama.getRefId());
                for (KullaniciRol kr : roller) {
                    kullaniciRepository.findById(kr.getKullaniciId())
                            .ifPresent(u -> names.add(u.getAdSoyad() + " (Rol: " + atama.getRefId() + ")"));
                }
            }
        }
        return names.stream().distinct().collect(Collectors.toList());
    }
}