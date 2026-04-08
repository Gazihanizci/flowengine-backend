package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flow_baslatma_istekleri")
@Getter
@Setter
public class FlowBaslatmaIstek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long akisId;
    // FlowBaslatmaIstek.java içine eklenecek alanlar:
    private Long parentSurecId;
    private Long parentAdimId;
    private Integer resumeAdimSirasi;

    private Long isteyenKullaniciId;

    private String durum; // BEKLIYOR / ONAY / RED

    private LocalDateTime olusturmaTarihi;
}