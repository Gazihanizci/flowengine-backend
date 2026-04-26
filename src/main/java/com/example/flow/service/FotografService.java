package com.example.flow.service;

import com.example.flow.entity.Fotograf;
import com.example.flow.repository.FotografRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FotografService {

    private final FotografRepository fotografRepository;

    public Fotograf upload(
            MultipartFile file,
            Long userId,
            Long surecId,
            Long adimId,
            Long aksiyonId
    ) {

        try {

            // 🔥 1. VALIDATION
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Fotoğraf boş olamaz");
            }

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Sadece fotoğraf yüklenebilir");
            }

            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                throw new RuntimeException("Fotoğraf max 5MB olabilir");
            }

            // 🔥 2. KLASÖR (SADECE FOTOĞRAF)
            String uploadDir = System.getProperty("user.dir")
                    + File.separator + "uploads"
                    + File.separator + "images";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 🔥 3. DOSYA ADI
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalName;

            // 🔥 4. PATH
            String fullPath = uploadDir + File.separator + fileName;

            File target = new File(fullPath);

            // 🔥 5. DISKE YAZ
            file.transferTo(target);

            // 🔥 6. DB KAYIT
            Fotograf fotograf = new Fotograf();
            fotograf.setFotografAdi(originalName);
            fotograf.setSaklananAd(fileName);
            fotograf.setFotografYolu(fullPath);
            fotograf.setFotografTipi(contentType);
            fotograf.setFotografBoyutu(file.getSize());

            fotograf.setYukleyenKullaniciId(userId);
            fotograf.setSurecId(surecId);
            fotograf.setAdimId(adimId);
            fotograf.setAksiyonId(aksiyonId);

            return fotografRepository.save(fotograf);

        } catch (IOException e) {
            throw new RuntimeException("Fotoğraf diske yazılamadı: " + e.getMessage());
        }
    }
}