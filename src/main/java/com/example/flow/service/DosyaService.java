package com.example.flow.service;

import com.example.flow.entity.Dosya;
import com.example.flow.repository.DosyaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DosyaService {

    private final DosyaRepository dosyaRepository;

    public Dosya upload(
            MultipartFile file,
            Long userId,
            Long surecId,
            Long adimId,
            Long aksiyonId
    ) {

        try {

            // 🔥 1. NULL / EMPTY CHECK
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Yüklenen dosya boş");
            }

            // 🔥 2. KLASÖR YOLU
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 🔥 garanti klasör oluştur
            }

            // 🔥 3. DOSYA ADI
            String originalName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalName;

            // 🔥 4. TAM PATH
            String fullPath = uploadDir + File.separator + fileName;

            File target = new File(fullPath);

            // 🔥 5. DOSYA KAYDET
            file.transferTo(target);

            // 🔥 6. ENTITY
            Dosya dosya = new Dosya();
            dosya.setDosyaAdi(originalName);
            dosya.setSaklananAd(fileName);
            dosya.setDosyaYolu(fullPath);
            dosya.setDosyaTipi(file.getContentType());
            dosya.setDosyaBoyutu(file.getSize());
            dosya.setYukleyenKullaniciId(userId);

            dosya.setSurecId(surecId);
            dosya.setAdimId(adimId);
            dosya.setAksiyonId(aksiyonId);

            return dosyaRepository.save(dosya);

        } catch (IOException e) {
            e.printStackTrace(); // 🔥 DEBUG
            throw new RuntimeException("Dosya diske yazılamadı: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 DEBUG
            throw new RuntimeException("Dosya upload hatası: " + e.getMessage());
        }
    }
}