package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.font.PdfEncodings;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final AkisRepository akisRepository;
    private final SurecRepository surecRepository;
    private final SurecHareketRepository hareketRepository;
    private final FormVeriRepository formVeriRepository;
    private final KullaniciRepository kullaniciRepository;
    private final FormBileseniRepository bilesenRepository;

    private String getAksiyonAdi(Long aksiyonId) {
        if (aksiyonId == null) return "Bilinmiyor";
        switch (aksiyonId.intValue()) {
            case 1: return "ONAYLANDI";
            case 2: return "KAYDEDİLDİ";
            case 3: return "REDDEDİLDİ";
            default: return "Bilinmeyen Aksiyon";
        }
    }

    public String generate(Long surecId) {
        try {
            String fileName = "rapor_" + surecId + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font;
            PdfFont boldFont;

            try {
                // 1. Ana fontu yükle (NotoSans-Regular)
                byte[] regularBytes = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream().readAllBytes();
                font = PdfFontFactory.createFont(regularBytes, PdfEncodings.IDENTITY_H);

                // 2. Bold fontu kontrol et ve yükle
                ClassPathResource boldRes = new ClassPathResource("fonts/NotoSans-Bold.ttf");
                if (boldRes.exists()) {
                    boldFont = PdfFontFactory.createFont(boldRes.getInputStream().readAllBytes(), PdfEncodings.IDENTITY_H);
                } else {
                    // Bold dosyası yoksa normal olanı kullan
                    boldFont = font;
                }
            } catch (Exception e) {
                // HATA ALINAN YER BURASIYDI: Sabit yerine String "Cp1254" kullanıyoruz
                font = PdfFontFactory.createFont(StandardFonts.HELVETICA, "Cp1254");
                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, "Cp1254");
            }

            document.setFont(font);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            // --- BAŞLIK ---
            document.add(new Paragraph("FLOW RAPORU").setFont(boldFont).setFontSize(14));
            document.add(new Paragraph("Süreç ID: " + surecId));

            // --- AKIŞ BİLGİSİ ---
            AkisSurec surec = surecRepository.findById(surecId).orElse(null);
            if (surec != null) {
                Akis akis = akisRepository.findById(surec.getAkisId()).orElse(null);
                if (akis != null) {
                    document.add(new Paragraph("Akış Adı: " + akis.getAkisAdi()));
                    document.add(new Paragraph("Açıklama: " + akis.getAciklama()));
                }
            }

            document.add(new Paragraph("\n"));

            // --- HAREKETLER ---
            List<SurecHareket> hareketler = hareketRepository.findBySurecIdOrderByTarihAsc(surecId);
            for (SurecHareket h : hareketler) {
                Kullanici user = kullaniciRepository.findById(h.getYapanKullaniciId()).orElse(null);
                String userName = (user != null) ? user.getAdSoyad() : "Bilinmeyen";
                String aksiyonAdi = getAksiyonAdi(h.getAksiyonId());

                document.add(new Paragraph("Kullanıcı: " + userName).setFont(boldFont));
                document.add(new Paragraph("İşlem: " + aksiyonAdi + " | Tarih: " + h.getTarih().format(formatter)));

                // --- FORM VERİLERİ ---
                List<FormVeri> veriler = formVeriRepository.findBySurecId(surecId);
                for (FormVeri fv : veriler) {
                    FormBileseni b = bilesenRepository.findById(fv.getBilesenId()).orElse(null);
                    String label = (b != null) ? b.getLabel() : "Alan";
                    document.add(new Paragraph("   • " + label + ": " + fv.getDeger()).setFontSize(10));
                }
                document.add(new Paragraph("--------------------------------------------------"));
            }

            document.close();
            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("PDF raporu oluşturulurken hata oluştu: " + e.getMessage(), e);
        }
    }
}