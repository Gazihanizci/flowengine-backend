package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "adim_gecis_kurallari")
@Getter
@Setter
public class AdimGecisKural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gecis_id")
    private Long gecisId;

    // 🔗 mevcut adım
    @Column(name = "adim_id", nullable = false)
    private Long adimId;

    // 🔘 hangi buton / aksiyon
    @Column(name = "aksiyon_id", nullable = false)
    private Long aksiyonId;

    // 🔥 koşullu geçiş için (opsiyonel)
    @Column(name = "kosul_bilesen_id")
    private Long kosulBilesenId;

    // =, >, < vs.
    @Column(name = "operator")
    private String operator;

    // karşılaştırılacak değer
    @Column(name = "kosul_deger")
    private String kosulDeger;

    // 🚀 gideceği adım
    @Column(name = "sonraki_adim_id", nullable = false)
    private Long sonrakiAdimId;
}