package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

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
    @ElementCollection
    @CollectionTable(name = "flow_istek_kullanicilar", joinColumns = @JoinColumn(name = "istek_id"))
    @Column(name = "kullanici_id")
    private Set<Long> assignedUserIds;
}