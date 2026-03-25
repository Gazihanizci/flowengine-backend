package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bilesen_secenekleri")
public class BilesenSecenegi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "secenek_id")
    private Long secenekId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bilesen_id", nullable = false)
    private FormBileseni bilesen;

    @Column(name = "etiket", nullable = false)
    private String etiket;

    @Column(name = "deger", nullable = false)
    private String deger;
}