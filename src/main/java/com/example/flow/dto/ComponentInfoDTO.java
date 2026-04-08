package com.example.flow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ComponentInfoDTO {
    private String etiket;
    private String bilesenTipi;
    private List<String> yetkiliIsimleri; // "Gazih (User)", "Admin (Role)" gibi
}