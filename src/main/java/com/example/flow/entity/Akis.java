package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "akislar")
public class Akis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "akis_id")
    private Long akisId;

    @Column(name = "akis_adi", nullable = false)
    private String akisAdi;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "aktif")
    private Boolean aktif = true;
}