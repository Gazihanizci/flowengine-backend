package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bildirimler")
@Getter
@Setter
public class Bildirim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bildirim_id")
    private Long bildirimId;

    @Column(name = "kullanici_id", nullable = false)
    private Long kullaniciId;

    @Column(name = "baslik")
    private String baslik;

    @Column(name = "mesaj", columnDefinition = "TEXT")
    private String mesaj;

    @Column(name = "tip", length = 50)
    private String tip;

    @Column(name = "okundu")
    private Boolean okundu;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi;

    @Column(name = "referans_surec_id")
    private Long referansSurecId;

    @Column(name = "referans_adim_id")
    private Long referansAdimId;

    @Column(name = "referans_istek_id")
    private Long referansIstekId;

    // 🔥 YENİ
    @Column(name = "gonderen_kullanici_id")
    private Long gonderenKullaniciId;

    @Column(name = "akis_id")
    private Long akisId;
}