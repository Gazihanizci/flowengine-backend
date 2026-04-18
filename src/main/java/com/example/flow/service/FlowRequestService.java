package com.example.flow.service;

import com.example.flow.dto.FlowStartResponse;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        istek.setDurum("ONAY");
        istekRepository.save(istek);

        // 🔥 EN KRİTİK SATIR
        FlowStartResponse response = flowStartService.startFlow(
                istek.getAkisId(),
                istek.getIsteyenKullaniciId(),
                true,
                istek.getAssignedUserIds()
        );

        // 🔥 PARENT BAĞLAMA
        if (istek.getParentSurecId() != null && response.getSurecId() != null) {

            AkisSurec child = surecRepository.findById(response.getSurecId())
                    .orElseThrow(() -> new RuntimeException("Child süreç bulunamadı"));

            child.setParentSurecId(istek.getParentSurecId());
            child.setParentAdimId(istek.getParentAdimId());
            child.setResumeAdimSirasi(istek.getResumeAdimSirasi());

            surecRepository.save(child);

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
                parent.setBitisTarihi(java.time.LocalDateTime.now());
                surecRepository.save(parent);
            });
        }
    }
}