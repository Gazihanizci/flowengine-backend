package com.example.flow.repository;

import com.example.flow.entity.FormVeri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormVeriRepository
        extends JpaRepository<FormVeri, Long> {
    List<FormVeri> findBySurecId(Long surecId);

}