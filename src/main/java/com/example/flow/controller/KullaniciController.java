package com.example.flow.controller;

import com.example.flow.dto.KullaniciMeResponse;
import com.example.flow.service.KullaniciService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kullanicilar")
@RequiredArgsConstructor
public class KullaniciController {

    private final KullaniciService kullaniciService;

    @GetMapping("/me")
    public List<KullaniciMeResponse> getMyInfo() {
        return kullaniciService.getMyInfo();
    }
}