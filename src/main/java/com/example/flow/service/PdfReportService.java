package com.example.flow.service;

import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final SurecRepository surecRepository;
    private final SurecHareketRepository hareketRepository;
    private final FormVeriRepository formVeriRepository;
    private final KullaniciRepository kullaniciRepository;
    private final FormBileseniRepository bilesenRepository;
    private String getAksiyonAdi(Long aksiyonId) {

        if (aksiyonId == null) return "Bilinmiyor";

        switch (aksiyonId.intValue()) {
            case 1:
                return "ONAYLANDI";
            case 2:
                return "KAYDEDİLDİ";
            case 3:
                return "REDDEDİLDİ";
            default:
                return "Bilinmeyen Aksiyon";
        }
    }
    public String generate(Long surecId) {

        try {
            String fileName = "rapor_" + surecId + ".pdf";

            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            // 🔥 BAŞLIK
            document.add(new Paragraph("FLOW RAPORU"));
            document.add(new Paragraph("Süreç ID: " + surecId));
            document.add(new Paragraph(" "));

            // 🔥 HAREKETLER
            List<SurecHareket> hareketler =
                    hareketRepository.findBySurecIdOrderByTarihAsc(surecId);

            for (SurecHareket h : hareketler) {

                Kullanici user = kullaniciRepository
                        .findById(h.getYapanKullaniciId())
                        .orElse(null);

                String userName = user != null ? user.getAdSoyad() : "Bilinmeyen";

                String aksiyonAdi = getAksiyonAdi(h.getAksiyonId());

                document.add(new Paragraph(
                        "Kullanıcı: " + userName
                ));
                document.add(new Paragraph(
                        "İşlem: " + aksiyonAdi
                ));
                document.add(new Paragraph(
                        "Tarih: " + h.getTarih().format(formatter)
                ));

                // 🔥 FORM VERİLERİ
                List<FormVeri> veriler =
                        formVeriRepository.findBySurecId(surecId);

                for (FormVeri fv : veriler) {

                    FormBileseni b = bilesenRepository
                            .findById(fv.getBilesenId())
                            .orElse(null);

                    String label = b != null ? b.getLabel() : "Field";

                    document.add(new Paragraph(
                            "   - " + label + ": " + fv.getDeger()
                    ));
                }

                document.add(new Paragraph(" "));
            }

            document.close();

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("PDF oluşturulamadı", e);
        }

    }
}