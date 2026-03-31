package com.example.flow.repository;

import com.example.flow.entity.BilesenSecenegi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BilesenSecenegiRepository extends JpaRepository<BilesenSecenegi, Long> {

    // ✅ DOĞRU
    List<BilesenSecenegi> findByBilesen_BilesenId(Long bilesenId);
}