package com.example.flow.controller;

import com.example.flow.dto.FlowStartRequest;
import com.example.flow.dto.FlowStartResponse;
import com.example.flow.security.CurrentUser;
import com.example.flow.service.FlowRequestService;
import com.example.flow.service.FlowStartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flow")
@RequiredArgsConstructor
public class FlowController {

    private final FlowStartService flowStartService;
    private final FlowRequestService flowRequestService;
    private final CurrentUser currentUser;

    // 🔥 FLOW BAŞLAT
    @PostMapping("/start")
    public FlowStartResponse start(@RequestBody FlowStartRequest request) {

        Long userId = currentUser.id();

        // 🔥 NULL KONTROL (KRİTİK)
        if (userId == null) {
            System.out.println("UYARI: userId NULL geliyor!");
            userId = -1L; // fallback (sistemi bozmaz)
        }

        return flowStartService.startFlow(
                request.getAkisId(),
                userId
        );
    }

    // 🔥 BAŞLATMA İSTEĞİNİ ONAYLA
    @PostMapping("/requests/{requestId}/approve")
    public String approve(@PathVariable Long requestId) {

        flowRequestService.approve(requestId);

        return "Flow başlatıldı";
    }

    // 🔥 BAŞLATMA İSTEĞİNİ REDDET
    @PostMapping("/requests/{requestId}/reject")
    public String reject(@PathVariable Long requestId) {

        flowRequestService.reject(requestId);

        return "İstek reddedildi";
    }
}