package com.example.flow.repository;

import com.example.flow.entity.AkisSurec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AkisSurecRepository extends JpaRepository<AkisSurec, Long> {

    Optional<AkisSurec> findBySurecId(Long surecId);
}