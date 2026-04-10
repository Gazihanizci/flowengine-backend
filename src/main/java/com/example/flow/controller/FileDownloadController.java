package com.example.flow.controller;

import com.example.flow.entity.Dosya;
import com.example.flow.repository.DosyaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/file-download") // 🔥 DEĞİŞTİ
@RequiredArgsConstructor
public class FileDownloadController {

    private final DosyaRepository dosyaRepository;

    @GetMapping("/{id}") // 🔥 DEĞİŞTİ
    public ResponseEntity<Resource> download(@PathVariable Long id) {

        try {

            Dosya dosya = dosyaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dosya bulunamadı"));

            File file = new File(dosya.getDosyaYolu());

            if (!file.exists()) {
                throw new RuntimeException("Dosya diskte bulunamadı");
            }

            Resource resource = new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + dosya.getDosyaAdi() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE,
                            dosya.getDosyaTipi() != null
                                    ? dosya.getDosyaTipi()
                                    : "application/octet-stream")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Dosya indirilemedi", e);
        }
    }
}