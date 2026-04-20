package com.example.flow.service;

import com.example.flow.dto.FlowStartResponse;
import com.example.flow.entity.AkisSurec;
import com.example.flow.entity.FlowBaslatmaIstek;
import com.example.flow.repository.FlowBaslatmaIstekRepository;
import com.example.flow.repository.SurecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FlowRequestService {

    private final FlowBaslatmaIstekRepository istekRepository;
    private final SurecRepository surecRepository;
    private final FlowStartService flowStartService;

    @Transactional
    public void approve(Long requestId) {

        FlowBaslatmaIstek istek = istekRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İzin isteği bulunamadı"));

        // Aynı isteğin tekrar çalışmasını engelle
        if ("ONAY".equalsIgnoreCase(istek.getDurum())) {
            return;
        }

        istek.setDurum("ONAY");
        istekRepository.save(istek);

        // 🔥 HIBERNATE FIX (ÇOK KRİTİK)
        Set<Long> assignedUserIdsCopy = null;
        if (istek.getAssignedUserIds() != null) {
            assignedUserIdsCopy = new HashSet<>(istek.getAssignedUserIds());
        }

        // 🔥 FLOW BAŞLAT
        FlowStartResponse response = flowStartService.startFlow(
                istek.getAkisId(),
                istek.getIsteyenKullaniciId(),
                true, // forceStart kalacak
                assignedUserIdsCopy
        );

        // 🔥 PARENT BAĞLAMA
        if (istek.getParentSurecId() != null && response.getSurecId() != null) {

            AkisSurec child = surecRepository.findById(response.getSurecId())
                    .orElseThrow(() -> new RuntimeException("Child süreç bulunamadı"));

            child.setParentSurecId(istek.getParentSurecId());
            child.setParentAdimId(istek.getParentAdimId());
            child.setResumeAdimSirasi(istek.getResumeAdimSirasi());

            surecRepository.save(child);

            // 🔥 PARENT DURUM GÜNCELLE
            surecRepository.findById(istek.getParentSurecId()).ifPresent(parent -> {
                parent.setDurum("WAITING_EXTERNAL");
                surecRepository.save(parent);
            });
        }
    }

    @Transactional
    public void reject(Long requestId) {

        FlowBaslatmaIstek istek = istekRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İzin isteği bulunamadı"));

        istek.setDurum("RED");
        istekRepository.save(istek);

        if (istek.getParentSurecId() != null) {
            surecRepository.findById(istek.getParentSurecId()).ifPresent(parent -> {
                parent.setDurum("REDDEDILDI");
                parent.setBitisTarihi(LocalDateTime.now());
                surecRepository.save(parent);
            });
        }
    }
}