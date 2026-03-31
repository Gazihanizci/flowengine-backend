package com.example.flow.repository;

import com.example.flow.entity.AdimGecisKural;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdimGecisKuralRepository
        extends JpaRepository<AdimGecisKural, Long> {

    Optional<AdimGecisKural>
    findFirstByAdimIdAndAksiyonId(Long adimId, Long aksiyonId);

    List<AdimGecisKural>
    findByAdimIdAndAksiyonId(Long adimId, Long aksiyonId);
}