package com.example.flow.repository;

import com.example.flow.entity.FlowBaslatmaIstek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowBaslatmaIstekRepository
        extends JpaRepository<FlowBaslatmaIstek, Long> {
}