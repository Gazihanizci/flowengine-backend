package com.example.flow.repository;

import com.example.flow.entity.Dosya;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DosyaRepository extends JpaRepository<Dosya, Long> {
}