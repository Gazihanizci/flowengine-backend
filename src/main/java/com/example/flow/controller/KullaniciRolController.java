package com.example.flow.controller;

import com.example.flow.dto.KullaniciRolResponse;
import com.example.flow.service.KullaniciRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kullanici-rolleri")
@RequiredArgsConstructor
public class KullaniciRolController {

    private final KullaniciRolService kullaniciRolService;

    @GetMapping
    public List<KullaniciRolResponse> getAll() {
        return kullaniciRolService.getAll();
    }
}