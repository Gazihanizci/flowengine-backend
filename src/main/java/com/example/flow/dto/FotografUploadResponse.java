package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FotografUploadResponse {

    private Long fotografId;
    private String fotografAdi;
    private String imageUrl;
}