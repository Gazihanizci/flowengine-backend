package com.example.flow.repository;

import com.example.flow.entity.FormVeri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormVeriRepository
        extends JpaRepository<FormVeri, Long> {
    List<FormVeri> findBySurecId(Long surecId);
    Optional<FormVeri> findBySurecIdAndBilesenId(Long surecId, Long bilesenId);


}