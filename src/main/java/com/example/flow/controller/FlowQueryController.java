package com.example.flow.controller;

import com.example.flow.entity.Akis;
import com.example.flow.repository.AkisRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
@CrossOrigin("*")
public class FlowQueryController {

    private final AkisRepository akisRepository;

    public FlowQueryController(AkisRepository akisRepository) {
        this.akisRepository = akisRepository;
    }

    // 🔥 TÜM FLOWLARI GETİR
    @GetMapping
    public List<Akis> getAllFlows() {
        return akisRepository.findAll();
    }
}