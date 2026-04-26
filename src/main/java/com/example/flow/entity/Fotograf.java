package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fotograflar")
@Getter
@Setter
public class Fotograf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fotografId;

    private String fotografAdi;
    private String saklananAd;
    private String fotografYolu;
    private String fotografTipi;
    private Long fotografBoyutu;

    private Long yukleyenKullaniciId;

    private Long surecId;
    private Long adimId;
    private Long aksiyonId;
}