package com.example.flow.controller;

import com.example.flow.dto.FlowMapResponse;
import com.example.flow.service.FlowMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flow-map")
@RequiredArgsConstructor
public class FlowMapController {

    private final FlowMapService flowMapService;

    @GetMapping("/{akisId}")
    public FlowMapResponse getMap(@PathVariable Long akisId) {
        return flowMapService.getFullFlowStructure(akisId);
    }
}