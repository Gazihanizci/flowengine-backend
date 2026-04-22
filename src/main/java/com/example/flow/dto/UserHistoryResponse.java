package com.example.flow.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserHistoryResponse {

    private Long surecId;
    private String akisAdi;
    private String adimAdi;

    private String aksiyon;
    private String formIcerik;
    private String aciklama;
    private String tarih;
}