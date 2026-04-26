package com.example.flow.controller;

import com.example.flow.dto.FotografUploadResponse;
import com.example.flow.entity.Fotograf;
import com.example.flow.repository.FotografRepository;
import com.example.flow.service.FotografService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/fotograflar")
@RequiredArgsConstructor
public class FotografController {

    private final FotografService fotografService;
    private final FotografRepository fotografRepository;

    // 🔥 FOTOĞRAF YÜKLE
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long surecId,
            @RequestParam Long adimId,
            @RequestParam Long aksiyonId,
            @RequestParam Long userId
    ) {

        try {

            Fotograf fotograf = fotografService.upload(
                    file,
                    userId,
                    surecId,
                    adimId,
                    aksiyonId
            );

            return ResponseEntity.ok(
                    new FotografUploadResponse(
                            fotograf.getFotografId(),
                            fotograf.getFotografAdi(),
                            "/api/fotograflar/view/" + fotograf.getFotografId()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Fotoğraf yükleme hatası: " + e.getMessage());
        }
    }

    // 🔥 FOTOĞRAF GÖRÜNTÜLE (DOWNLOAD DEĞİL)
    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> view(@PathVariable Long id) {

        try {

            Fotograf fotograf = fotografRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fotoğraf bulunamadı"));

            File file = new File(fotograf.getFotografYolu());

            Resource resource = new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, fotograf.getFotografTipi())
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Fotoğraf görüntülenemedi");
        }
    }
}