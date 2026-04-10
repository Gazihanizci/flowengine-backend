package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResponse {

    private Long dosyaId;
    private String fileName;
    private String downloadUrl;
}