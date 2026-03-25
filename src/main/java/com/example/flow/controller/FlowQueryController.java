package com.example.flow.controller;

import com.example.flow.dto.FlowListResponse;
import com.example.flow.service.FlowQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
@CrossOrigin("*")
public class FlowQueryController {

    private final FlowQueryService flowQueryService;

    public FlowQueryController(FlowQueryService flowQueryService) {
        this.flowQueryService = flowQueryService;
    }

    @GetMapping("/{id}")
    public Object getFlow(@PathVariable Long id) {
        return flowQueryService.getFlowFull(id);
    }
    @GetMapping
    public List<FlowListResponse> getAll() {
        return flowQueryService.getFlows();
    }
}