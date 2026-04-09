package com.example.flow.controller;

import com.example.flow.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfReportController {

    private final PdfReportService pdfReportService;

    // 🔥 PDF oluştur + indir
    @GetMapping("/generate/{surecId}")
    public ResponseEntity<InputStreamResource> generate(@PathVariable Long surecId) {

        try {
            String fileName = pdfReportService.generate(surecId);

            FileInputStream file = new FileInputStream(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(file));

        } catch (Exception e) {
            throw new RuntimeException("PDF indirilemedi", e);
        }
    }
}