package com.example.flow.controller;

import com.example.flow.entity.FlowBaslatmaYetki;
import com.example.flow.service.FlowBaslatmaYetkiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flow-yetki")
@RequiredArgsConstructor
public class FlowBaslatmaYetkiController {

    private final FlowBaslatmaYetkiService service;

    // 🔥 TÜMÜNÜ GETİR
    @GetMapping
    public List<FlowBaslatmaYetki> getAll() {
        return service.getAll();
    }

    // 🔥 FLOW'A GÖRE GETİR
    @GetMapping("/{akisId}")
    public List<FlowBaslatmaYetki> getByAkisId(@PathVariable Long akisId) {
        return service.getByAkisId(akisId);
    }

    // 🔥 EKLE
    @PostMapping
    public FlowBaslatmaYetki add(
            @RequestParam Long akisId,
            @RequestParam String tip,
            @RequestParam Long refId
    ) {
        return service.add(akisId, tip, refId);
    }

    // 🔥 SİL
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Silindi";
    }
}