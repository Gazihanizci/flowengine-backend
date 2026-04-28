package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_verileri")
@Getter
@Setter
public class FormVeri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 hangi süreç
    @Column(name = "surec_id", nullable = false)
    private Long surecId;

    // 🔗 hangi bileşen (input, radio vs)
    @Column(name = "bilesen_id", nullable = false)
    private Long bilesenId;

    // 🔥 kullanıcının girdiği değer
    @Column(name = "deger", columnDefinition = "TEXT")
    private String deger;

    // 🔗 kim kaydetti
    @Column(name = "kaydeden_kullanici_id")
    private Long kaydedenKullaniciId;

    // 🕒 ne zaman kaydedildi
    @Column(name = "kayit_tarihi")
    private LocalDateTime kayitTarihi;

    @Column(name = "adim_id")
    private Long adimId;
}