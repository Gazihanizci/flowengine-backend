package com.example.flow.repository;

import com.example.flow.entity.AkisSurec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurecRepository extends JpaRepository<AkisSurec, Long> {
}