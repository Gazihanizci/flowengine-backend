package com.example.flow.controller;

import com.example.flow.service.BildirimIslemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bildirim-islemleri")
@RequiredArgsConstructor
public class BildirimIslemController {

    private final BildirimIslemService bildirimIslemService;

    @PutMapping("/{bildirimId}/okundu")
    public String okunduYap(@PathVariable Long bildirimId) {
        bildirimIslemService.okunduYap(bildirimId);
        return "Bildirim okundu olarak işaretlendi";
    }
}