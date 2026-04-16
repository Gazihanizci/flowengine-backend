package com.example.flow.service;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.repository.KullaniciRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KullaniciRolService {

    private final KullaniciRolRepository repository;

    public List<KullaniciRolResponse> getAll() {

        return repository.findAllUsersWithRoles()
                .stream()
                .map(row -> {

                    KullaniciRolResponse res = new KullaniciRolResponse();

                    // 🔹 NULL SAFE
                    res.setKullaniciId(((Number) row[0]).longValue());
                    res.setAdSoyad((String) row[1]);
                    res.setEmail((String) row[2]);

                    // 🔥 BURASI KRİTİK (NULL CHECK)
                    if (row[3] != null) {
                        res.setRolId(((Number) row[3]).longValue());
                        res.setRolAdi((String) row[4]);
                    } else {
                        res.setRolId(null);
                        res.setRolAdi("ROL YOK");
                    }

                    return res;
                })
                .collect(Collectors.toList());
    }
}