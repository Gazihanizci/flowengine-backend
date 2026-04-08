package com.example.flow.service;

import com.example.flow.entity.Bildirim;
import com.example.flow.repository.BildirimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BildirimService {

    private final BildirimRepository bildirimRepository;

    public List<Bildirim> kullanicininBildirimleri(Long kullaniciId) {
        return bildirimRepository.findByKullaniciIdOrderByOlusturmaTarihiDesc(kullaniciId);
    }

    public long okunmamisSayisi(Long kullaniciId) {
        return bildirimRepository.countByKullaniciIdAndOkunduFalse(kullaniciId);
    }
}