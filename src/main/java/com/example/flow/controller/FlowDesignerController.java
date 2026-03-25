package com.example.flow.controller;

import com.example.flow.dto.FlowSaveRequest;
import com.example.flow.dto.FlowSaveResponse;
import com.example.flow.service.FlowDesignerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/designer/flows")
@CrossOrigin(origins = "*")
public class FlowDesignerController {

    private final FlowDesignerService flowDesignerService;

    public FlowDesignerController(FlowDesignerService flowDesignerService) {
        this.flowDesignerService = flowDesignerService;
    }

    @PostMapping
    public ResponseEntity<FlowSaveResponse> saveFlow(@Valid @RequestBody FlowSaveRequest request) {
        FlowSaveResponse response = flowDesignerService.saveFlow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}