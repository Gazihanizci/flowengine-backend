package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder // 🔥 EKLE

public class KullaniciRolResponse {

    private Long kullaniciId;
    private String adSoyad;
    private String email;
    private Long rolId;
    private String rolAdi;
    public KullaniciRolResponse() {}

    public Long getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(Long kullaniciId) { this.kullaniciId = kullaniciId; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
}