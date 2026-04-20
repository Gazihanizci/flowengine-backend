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

    private boolean hasPermission(Long userId, Long bilesenId, String yetkiTipi) {

        List<FormBileseniAtama> atamalar = atamaRepository.findByBilesenId(bilesenId);

        // Hiç atama yoksa serbest
        if (atamalar.isEmpty()) {
            return true;
        }

        for (FormBileseniAtama a : atamalar) {

            boolean yetkiUygun =
                    yetkiTipi.equalsIgnoreCase(a.getYetkiTipi()) ||
                            ("VIEW".equalsIgnoreCase(yetkiTipi) && "EDIT".equalsIgnoreCase(a.getYetkiTipi()));

            if (!yetkiUygun) {
                continue;
            }

            if ("USER".equalsIgnoreCase(a.getTip()) && a.getRefId().equals(userId)) {
                return true;
            }

            if ("ROLE".equalsIgnoreCase(a.getTip())) {
                List<KullaniciRol> roller = kullaniciRolRepository.findByKullaniciId(userId);

                for (KullaniciRol rol : roller) {
                    if (rol.getRolId().equals(a.getRefId())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void validateView(Long userId, Long bilesenId) {
        if (!hasPermission(userId, bilesenId, "VIEW")) {
            throw new RuntimeException("Bu alanı görme yetkin yok!");
        }
    }

    public void validateEdit(Long userId, Long bilesenId) {
        if (!hasPermission(userId, bilesenId, "EDIT")) {
            throw new RuntimeException("Bu alanı düzenleme yetkin yok!");
        }
    }

    public boolean canView(Long userId, Long bilesenId) {
        return hasPermission(userId, bilesenId, "VIEW");
    }

    public boolean canEdit(Long userId, Long bilesenId) {
        return hasPermission(userId, bilesenId, "EDIT");
    }
}