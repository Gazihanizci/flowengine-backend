package com.example.flow.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurecListResponse {

    private Long surecId;
    private String akisAdi;
    private String akisAciklama;

    // 🔥 YENİ
    private String baslamaTarihi;
}