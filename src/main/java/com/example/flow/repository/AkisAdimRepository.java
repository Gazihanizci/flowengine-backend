package com.example.flow.repository;

import com.example.flow.entity.AkisAdim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AkisAdimRepository extends JpaRepository<AkisAdim, Long> {

    List<AkisAdim> findByAkis_AkisIdOrderByAdimSirasi(Long akisId);
}