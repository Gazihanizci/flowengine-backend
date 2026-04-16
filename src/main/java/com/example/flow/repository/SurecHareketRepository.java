package com.example.flow.repository;

import com.example.flow.entity.SurecHareket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurecHareketRepository extends JpaRepository<SurecHareket, Long> {

    List<SurecHareket> findBySurecIdOrderByTarihAsc(Long surecId);
    List<SurecHareket> findByYapanKullaniciIdOrderByTarihDesc(Long userId);
}