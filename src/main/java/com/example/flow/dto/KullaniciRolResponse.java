package com.example.flow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KullaniciRolResponse {

    private Long kullaniciId;
    private String adSoyad;
    private String email;
    private Long rolId;
    private String rolAdi;
}