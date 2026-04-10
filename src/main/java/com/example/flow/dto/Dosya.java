package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dosyalar")
@Getter
@Setter
public class Dosya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dosyaId;

    private String dosyaAdi;
    private String saklananAd;
    private String dosyaYolu;
    private String dosyaTipi;
    private Long dosyaBoyutu;

    private Long yukleyenKullaniciId;

    private Long surecId;
    private Long adimId;
    private Long aksiyonId;

    private LocalDateTime yuklenmeTarihi = LocalDateTime.now();
}