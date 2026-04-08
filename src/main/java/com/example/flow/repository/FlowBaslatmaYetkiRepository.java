package com.example.flow.repository;

import com.example.flow.entity.FlowBaslatmaYetki;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowBaslatmaYetkiRepository
        extends JpaRepository<FlowBaslatmaYetki, Long> {
    List<FlowBaslatmaYetki> findByAkisId(Long akisId);
    boolean existsByAkisIdAndTipAndRefId(Long akisId, String tip, Long refId);
}
