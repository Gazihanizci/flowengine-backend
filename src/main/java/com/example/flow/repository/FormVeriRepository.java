package com.example.flow.repository;

import com.example.flow.entity.FormVeri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormVeriRepository extends JpaRepository<FormVeri, Long> {

    // 🔥 TÜM VERİ (admin/debug)
    List<FormVeri> findBySurecId(Long surecId);

    // 🔥 ESKİ (KULLANMA)
    Optional<FormVeri> findBySurecIdAndBilesenId(Long surecId, Long bilesenId);

    // 🔥 YENİ (KULLANILACAK)
    Optional<FormVeri> findBySurecIdAndBilesenIdAndKaydedenKullaniciId(
            Long surecId,
            Long bilesenId,
            Long userId
    );

    // 🔥 KULLANICIYA ÖZEL OKUMA
    List<FormVeri> findBySurecIdAndKaydedenKullaniciId(
            Long surecId,
            Long userId
    );
}