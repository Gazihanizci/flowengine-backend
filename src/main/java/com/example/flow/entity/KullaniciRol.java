package com.example.flow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kullanici_rolleri")
@Getter
@Setter
public class KullaniciRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kullanici_id")
    private Long kullaniciId;

    @Column(name = "rol_id")
    private Long rolId;
}