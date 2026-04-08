package com.example.flow.controller;

import com.example.flow.entity.Bildirim;
import com.example.flow.security.CurrentUser;
import com.example.flow.service.BildirimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bildirimler")
@RequiredArgsConstructor
public class BildirimController {

    private final BildirimService bildirimService;
    private final CurrentUser currentUser;

    // 🔥 LOGIN USER BİLDİRİMLERİ
    @GetMapping("/me")
    public List<Bildirim> getMyNotifications() {

        Long userId = currentUser.id();

        return bildirimService.kullanicininBildirimleri(userId);
    }

    // 🔥 OKUNMAMIŞ SAYI
    @GetMapping("/me/okunmamis-sayi")
    public long getUnreadCount() {

        Long userId = currentUser.id();

        return bildirimService.okunmamisSayisi(userId);
    }
}