package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "akis_adimlari")
public class AkisAdim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adim_id")
    private Long adimId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "akis_id", nullable = false)
    private Akis akis;

    @Column(name = "adim_adi", nullable = false)
    private String adimAdi;

    @Column(name = "adim_sirasi", nullable = false)
    private Integer adimSirasi;
}