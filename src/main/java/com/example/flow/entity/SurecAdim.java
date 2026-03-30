package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "surec_adimlari")
@Getter
@Setter
public class SurecAdim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long surecId;

    private Long adimId;

    private Long atananKullaniciId;

    private String durum;

    private Boolean tamamlandiMi;

    private LocalDateTime baslamaTarihi;

    private LocalDateTime bitisTarihi;
}