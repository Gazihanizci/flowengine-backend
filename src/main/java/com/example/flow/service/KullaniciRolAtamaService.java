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

    public List<KullaniciRolResponse> getAll() {
        return repository.findAll().stream().map(r -> {
            KullaniciRolResponse res = new KullaniciRolResponse();
            res.setKullaniciId(r.getKullaniciId());
            res.setRolId(r.getRolId());
            return res;
        }).collect(Collectors.toList());
    }

    public void assignRole(Long kullaniciId, Long rolId) {

        KullaniciRol entity = new KullaniciRol();
        entity.setKullaniciId(kullaniciId);
        entity.setRolId(rolId);

        repository.save(entity);
    }

    public void removeRole(Long kullaniciId, Long rolId) {
        repository.deleteByKullaniciIdAndRolId(kullaniciId, rolId);
    }

    public void updateRole(Long kullaniciId, Long eskiRolId, Long yeniRolId) {

        KullaniciRol entity = repository
                .findByKullaniciIdAndRolId(kullaniciId, eskiRolId)
                .orElseThrow(() -> new RuntimeException("Rol bulunamadı"));

        entity.setRolId(yeniRolId);
        repository.save(entity);
    }

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