package com.example.flow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ComponentNodeDTO { // Mutlaka PUBLIC olmalı
    private String etiket;
    private String tip;
    private List<String> yetkiliIsimleri;
}