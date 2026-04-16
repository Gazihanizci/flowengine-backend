package com.example.flow.service;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.entity.KullaniciRol;
import com.example.flow.repository.KullaniciRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KullaniciRolAtamaService {

    private final KullaniciRolRepository repository;

    // 🔥 TÜM ATAMALAR
    public List<KullaniciRolResponse> getAll() {
        return repository.findAll().stream().map(r -> {
            KullaniciRolResponse res = new KullaniciRolResponse();
            res.setKullaniciId(r.getKullaniciId());
            res.setRolId(r.getRolId());
            return res;
        }).collect(Collectors.toList());
    }

    // 🔥 ROL ATA (DUPLICATE ENGELLİ)
    public void assignRole(Long kullaniciId, Long rolId) {

        boolean exists = repository
                .findByKullaniciIdAndRolId(kullaniciId, rolId)
                .isPresent();

        if (exists) {
            throw new RuntimeException("Bu rol zaten atanmış");
        }

        KullaniciRol entity = new KullaniciRol();
        entity.setKullaniciId(kullaniciId);
        entity.setRolId(rolId);

        repository.save(entity);
    }

    // 🔥 ROL SİL (GÜVENLİ)
    public void removeRole(Long kullaniciId, Long rolId) {

        KullaniciRol entity = repository
                .findByKullaniciIdAndRolId(kullaniciId, rolId)
                .orElseThrow(() -> new RuntimeException("Silinecek rol bulunamadı"));

        repository.delete(entity);
    }

    // 🔥 ROL GÜNCELLE
    public void updateRole(Long kullaniciId, Long eskiRolId, Long yeniRolId) {

        KullaniciRol entity = repository
                .findByKullaniciIdAndRolId(kullaniciId, eskiRolId)
                .orElseThrow(() -> new RuntimeException("Rol bulunamadı"));

        entity.setRolId(yeniRolId);
        repository.save(entity);
    }

    // 🔥 KULLANICIYA AİT ROLLER
    public List<KullaniciRolResponse> getRoles(Long kullaniciId) {

        return repository.findByKullaniciId(kullaniciId)
                .stream()
                .map(r -> {
                    KullaniciRolResponse res = new KullaniciRolResponse();
                    res.setKullaniciId(r.getKullaniciId());
                    res.setRolId(r.getRolId());
                    return res;
                })
                .collect(Collectors.toList());
    }
}