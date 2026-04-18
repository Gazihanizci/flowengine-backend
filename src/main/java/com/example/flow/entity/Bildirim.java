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

    private String baslik;

    @Column(columnDefinition = "TEXT")
    private String mesaj;

    private String tip;

    private Boolean okundu;

    private LocalDateTime olusturmaTarihi;

    private Long referansSurecId;
    private Long referansAdimId;
    private Long referansIstekId;

    // 🔥 YENİLER
    private Long gonderenKullaniciId;
    private Long akisId;
}