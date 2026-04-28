package com.example.flow.controller;

import com.example.flow.dto.*;
import com.example.flow.service.FlowEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flows/edit")
@RequiredArgsConstructor
public class FlowEditController {

    private final FlowEditService service;

    // =====================================================
    // 🔥 FLOW UPDATE
    // =====================================================
    @PutMapping("/{flowId}")
    public ResponseEntity<?> updateFlow(
            @PathVariable Long flowId,
            @RequestBody FlowUpdateRequest request
    ) {
        service.updateFlow(flowId, request);
        return ResponseEntity.ok("Flow güncellendi");
    }

    // =====================================================
    // 🔥 STEP UPDATE
    // =====================================================
    @PutMapping("/step/{stepId}")
    public ResponseEntity<?> updateStep(
            @PathVariable Long stepId,
            @RequestBody StepUpdateRequest request
    ) {
        service.updateStep(stepId, request);
        return ResponseEntity.ok("Step güncellendi");
    }

    // =====================================================
    // 🔥 FIELD UPDATE
    // =====================================================
    @PutMapping("/field/{fieldId}")
    public ResponseEntity<?> updateField(
            @PathVariable Long fieldId,
            @RequestBody FieldUpdateRequest request
    ) {
        service.updateField(fieldId, request);
        return ResponseEntity.ok("Field güncellendi");
    }

    // =====================================================
    // 🔥 STEP CREATE
    // =====================================================
    @PostMapping("/step/{flowId}")
    public ResponseEntity<?> createStep(
            @PathVariable Long flowId,
            @RequestBody StepUpdateRequest request
    ) {
        Long stepId = service.createStep(flowId, request);
        return ResponseEntity.ok(stepId);
    }

    // =====================================================
    // 🔥 FIELD CREATE
    // =====================================================
    @PostMapping("/field/{stepId}")
    public ResponseEntity<?> createField(
            @PathVariable Long stepId,
            @RequestBody FieldCreateRequest request
    ) {
        Long fieldId = service.createField(stepId, request);
        return ResponseEntity.ok(fieldId);
    }
}