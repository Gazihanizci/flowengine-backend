package com.example.flow.repository;

import com.example.flow.entity.SurecAdim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurecAdimRepository
        extends JpaRepository<SurecAdim, Long> {

    List<SurecAdim>
    findBySurecIdAndAdimId(Long surecId, Long adimId);
    List<SurecAdim> findByAtananKullaniciIdAndDurum(Long userId, String durum);
    long countBySurecIdAndAdimIdAndDurum(
            Long surecId,
            Long adimId,
            String durum
    );
}