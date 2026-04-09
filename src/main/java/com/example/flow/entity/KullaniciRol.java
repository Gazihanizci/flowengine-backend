package com.example.flow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kullanici_rolleri")
public class KullaniciRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kullaniciId;
    private Long rolId;

    // 🔥 BOŞ CONSTRUCTOR (ŞART)
    public KullaniciRol() {}

    // 🔥 GETTER - SETTER
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(Long kullaniciId) { this.kullaniciId = kullaniciId; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}