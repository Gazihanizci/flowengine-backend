package com.example.flow.controller;

import com.example.flow.entity.Dosya;
import com.example.flow.repository.DosyaRepository;
import com.example.flow.service.DosyaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class DosyaController {

    private final DosyaService dosyaService;
    private final DosyaRepository dosyaRepository;

    // 🔥 FILE UPLOAD (Swagger'da Dosya Seçme Butonunu Aktif Eden Versiyon)
    @Operation(summary = "Dosya yükle")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadFile(
            @Parameter(
                    description = "Yüklenecek dosya",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("file") MultipartFile file,

            @RequestParam Long surecId,
            @RequestParam Long adimId,
            @RequestParam Long aksiyonId,
            @RequestParam Long userId
    ) {
        try {
            Dosya dosya = dosyaService.upload(
                    file,
                    userId,
                    surecId,
                    adimId,
                    aksiyonId
            );

            return ResponseEntity.ok().body(
                    new UploadResponse(
                            dosya.getDosyaId(),
                            dosya.getDosyaAdi(),
                            "/api/files/download/" + dosya.getDosyaId()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Dosya yükleme hatası: " + e.getMessage());
        }
    }

    // 🔥 FILE DOWNLOAD
    @Operation(summary = "Dosya indir")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
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

    // 🔥 RESPONSE DTO
    public static class UploadResponse {
        private Long dosyaId;
        private String dosyaAdi;
        private String downloadUrl;

        public UploadResponse(Long dosyaId, String dosyaAdi, String downloadUrl) {
            this.dosyaId = dosyaId;
            this.dosyaAdi = dosyaAdi;
            this.downloadUrl = downloadUrl;
        }

        public Long getDosyaId() { return dosyaId; }
        public String getDosyaAdi() { return dosyaAdi; }
        public String getDownloadUrl() { return downloadUrl; }
    }
}