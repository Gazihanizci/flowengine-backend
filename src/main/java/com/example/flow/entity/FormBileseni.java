package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "form_bilesenleri")
public class FormBileseni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bilesen_id")
    private Long bilesenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(name = "bilesen_tipi", nullable = false)
    private String bilesenTipi;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "placeholder")
    private String placeholder;

    @Column(name = "zorunlu")
    private Boolean zorunlu = false;

    @Column(name = "sira_no")
    private Integer siraNo;
}