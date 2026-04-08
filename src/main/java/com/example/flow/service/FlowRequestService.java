package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FlowRequestService {

    private final FlowBaslatmaIstekRepository istekRepository;
    private final SurecRepository surecRepository;
    private final AkisAdimRepository akisAdimRepository;
    private final TaskService taskService;

    @Transactional
    public void approve(Long requestId) {
        FlowBaslatmaIstek istek = istekRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İzin isteği bulunamadı: " + requestId));

        // 1. İsteği onayla
        istek.setDurum("ONAY");
        istekRepository.save(istek);

        // 2. Alt Akışı (Child) oluştur ve PARENT bilgilerini bağla
        // 🔥 BURASI DB'DEKİ 'BABASI' KOLONUNU DOLDURUR:
        AkisSurec child = new AkisSurec();
        child.setAkisId(istek.getAkisId());
        child.setBaslatanKullaniciId(istek.getIsteyenKullaniciId());
        child.setDurum("RUNNING");
        child.setBaslamaTarihi(LocalDateTime.now());

        // Bağlantı bilgileri (İstekte kayıtlı olanlar)
        child.setParentSurecId(istek.getParentSurecId());
        child.setParentAdimId(istek.getParentAdimId());
        child.setResumeAdimSirasi(istek.getResumeAdimSirasi());

        AkisSurec savedChild = surecRepository.save(child);

        // 3. Alt akışın ilk adımını bul ve görevi oluştur
        AkisAdim firstStep = akisAdimRepository
                .findFirstByAkis_AkisIdOrderByAdimSirasiAsc(savedChild.getAkisId())
                .orElseThrow(() -> new RuntimeException("Alt akış adımı bulunamadı"));

        savedChild.setMevcutAdimId(firstStep.getAdimId());
        surecRepository.save(savedChild);

        taskService.createTasksForStep(savedChild.getSurecId(), firstStep.getAdimId());

        // 4. Ana akışın (Parent) durumunu bekleme moduna al
        if (istek.getParentSurecId() != null) {
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