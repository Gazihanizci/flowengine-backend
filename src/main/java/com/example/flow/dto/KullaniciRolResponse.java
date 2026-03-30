package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KullaniciRolResponse {

    private Long kullaniciId;
    private String adSoyad;
    private String email;
    private Long rolId;
    private String rolAdi;
}