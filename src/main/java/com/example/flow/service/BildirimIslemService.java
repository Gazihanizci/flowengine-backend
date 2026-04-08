package com.example.flow.service;

import com.example.flow.entity.Bildirim;
import com.example.flow.repository.BildirimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BildirimIslemService {

    private final BildirimRepository bildirimRepository;

    @Transactional
    public void okunduYap(Long bildirimId) {
        Bildirim bildirim = bildirimRepository.findById(bildirimId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));

        bildirim.setOkundu(true);
        bildirimRepository.save(bildirim);
    }
}