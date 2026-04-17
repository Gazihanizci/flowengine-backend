package com.example.flow.repository;

import com.example.flow.entity.AkisAdim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AkisAdimRepository extends JpaRepository<AkisAdim, Long> {

    List<AkisAdim> findByAkis_AkisIdOrderByAdimSirasi(Long akisId);
    Optional<AkisAdim> findFirstByAkis_AkisIdOrderByAdimSirasiAsc(Long akisId);
    Optional<AkisAdim> findFirstByAkis_AkisIdAndAdimSirasiGreaterThanOrderByAdimSirasiAsc(
            Long akisId,
            Integer adimSirasi
    );
    List<AkisAdim> findByAkis_AkisIdOrderByAdimSirasiAsc(Long akisId);
}