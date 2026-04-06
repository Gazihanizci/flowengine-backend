package com.example.flow.repository;

import com.example.flow.entity.SurecEventi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurecEventiRepository extends JpaRepository<SurecEventi, Long> {
    boolean existsByCorrelationId(String correlationId);
}