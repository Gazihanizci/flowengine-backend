package com.example.flow.repository;

import com.example.flow.entity.FormVeri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormVeriRepository extends JpaRepository<FormVeri, Long> {

    // 🔥 TÜM VERİ (debug)
    List<FormVeri> findBySurecId(Long surecId);

    // 🔥 FIELD + USER bazlı tek veri
    Optional<FormVeri> findBySurecIdAndBilesenIdAndKaydedenKullaniciId(
            Long surecId,
            Long bilesenId,
            Long userId
    );

    // 🔥 USER bazlı tüm veriler
    List<FormVeri> findBySurecIdAndKaydedenKullaniciId(
            Long surecId,
            Long userId
    );
}