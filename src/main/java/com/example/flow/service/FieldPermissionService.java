package com.example.flow.service;

import com.example.flow.entity.FormBileseniAtama;
import com.example.flow.entity.KullaniciRol;
import com.example.flow.repository.FormBileseniAtamaRepository;
import com.example.flow.repository.KullaniciRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldPermissionService {

    private final FormBileseniAtamaRepository atamaRepository;
    private final KullaniciRolRepository kullaniciRolRepository;

    public void validate(Long userId, Long bilesenId) {

        List<FormBileseniAtama> atamalar =
                atamaRepository.findByBilesenId(bilesenId);

        // 👉 hiç atama yoksa herkes doldurabilir
        if (atamalar.isEmpty()) return;

        boolean yetkili = false;

        for (FormBileseniAtama a : atamalar) {

            // USER bazlı
            if ("USER".equals(a.getTip())
                    && a.getRefId().equals(userId)) {
                yetkili = true;
            }

            // ROLE bazlı
            if ("ROLE".equals(a.getTip())) {

                List<KullaniciRol> roller =
                        kullaniciRolRepository.findByRolId(a.getRefId());

                for (KullaniciRol kr : roller) {
                    if (kr.getKullaniciId().equals(userId)) {
                        yetkili = true;
                    }
                }
            }
        }

        if (!yetkili) {
            throw new RuntimeException("Bu alanı doldurma yetkin yok!");
        }
    }
}