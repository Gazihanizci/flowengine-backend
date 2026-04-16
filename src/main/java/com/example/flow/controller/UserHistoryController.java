package com.example.flow.controller;

import com.example.flow.dto.UserHistoryResponse;
import com.example.flow.service.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class UserHistoryController {

    private final UserHistoryService service;

    // 🔥 USER ID PARAMETRESİ YOK
    @GetMapping("/my")
    public List<UserHistoryResponse> getMyHistory() {
        return service.getMyHistory();
    }
}