package com.example.flow.controller;

import com.example.flow.dto.SurecListResponse;
import com.example.flow.service.SurecQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surecler")
@RequiredArgsConstructor
public class SurecController {

    private final SurecQueryService service;

    // 🔥 TÜM SÜREÇLER
    @GetMapping
    public List<SurecListResponse> getAll() {
        return service.getAll();
    }
}