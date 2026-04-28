package com.example.flow.repository;

import com.example.flow.entity.FormBileseni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormBileseniRepository extends JpaRepository<FormBileseni, Long> {
    List<FormBileseni> findByForm_FormId(Long formId);
    Optional<FormBileseni> findById(Long id);
}