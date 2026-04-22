package com.example.flow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "surec_hareketleri")
public class SurecHareket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hareketId;

    private Long surecId;
    private Long adimId;
    private Long aksiyonId;
    private Long yapanKullaniciId;

    private String yapilanIslem;
    @Column(columnDefinition = "TEXT")
    private String aciklama;
    private LocalDateTime tarih;

    // GETTER SETTER
    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }
    public Long getHareketId() { return hareketId; }
    public void setHareketId(Long hareketId) { this.hareketId = hareketId; }

    public Long getSurecId() { return surecId; }
    public void setSurecId(Long surecId) { this.surecId = surecId; }

    public Long getAdimId() { return adimId; }
    public void setAdimId(Long adimId) { this.adimId = adimId; }

    public Long getAksiyonId() { return aksiyonId; }
    public void setAksiyonId(Long aksiyonId) { this.aksiyonId = aksiyonId; }

    public Long getYapanKullaniciId() { return yapanKullaniciId; }
    public void setYapanKullaniciId(Long yapanKullaniciId) { this.yapanKullaniciId = yapanKullaniciId; }

    public String getYapilanIslem() { return yapilanIslem; }
    public void setYapilanIslem(String yapilanIslem) { this.yapilanIslem = yapilanIslem; }

    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
}