package com.example.flow.controller;

import com.example.flow.dto.FlowStartRequest;
import com.example.flow.dto.FlowStartResponse;
import com.example.flow.security.CurrentUser;
import com.example.flow.service.FlowStartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flow")
@RequiredArgsConstructor
public class FlowController {

    private final FlowStartService flowStartService;
    private final CurrentUser currentUser; // ✅ inject

    @PostMapping("/start")
    public FlowStartResponse start(@RequestBody FlowStartRequest request) {

        Long userId = currentUser.id(); // ✅ artık doğru

        return flowStartService.startFlow(
                request.getAkisId(),
                userId
        );
    }
}