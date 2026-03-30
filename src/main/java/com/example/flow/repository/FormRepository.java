package com.example.flow.repository;

import com.example.flow.entity.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FormRepository extends JpaRepository<Form, Long> {
    @Query("SELECT f FROM Form f WHERE f.adim.adimId = :adimId")
    Optional<Form> findByAdimId(@Param("adimId") Long adimId);
}