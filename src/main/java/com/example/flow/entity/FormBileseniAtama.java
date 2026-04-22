package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "form_bileseni_atamalar")
public class FormBileseniAtama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bilesen_id")
    private Long bilesenId;
    @Column(name = "yetki_tipi")
    private String yetkiTipi;
    @Column(name = "tip")
    private String tip; // USER / ROLE

    @Column(name = "ref_id")
    private Long refId;

}