package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "akis_surecleri")
@Getter
@Setter
public class AkisSurec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surecId;

    private Long akisId;

    private Long baslatanKullaniciId;

    private Long mevcutAdimId;

    private String durum;

    private LocalDateTime baslamaTarihi;

    private LocalDateTime bitisTarihi;
    @Column(name = "parent_surec_id")
    private Long parentSurecId;

    @Column(name = "parent_adim_id")
    private Long parentAdimId;

    @Column(name = "resume_adim_sirasi")
    private Integer resumeAdimSirasi;
}