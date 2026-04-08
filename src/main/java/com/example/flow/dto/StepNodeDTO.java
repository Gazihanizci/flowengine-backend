package com.example.flow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StepNodeDTO {
    public String evre; // "1-ANA", "2-CHILD", "3-ANA-DEVAM"
    public Long adimId;
    public String adimAdi;
    public Integer sira;
    public List<ComponentNodeDTO> bilesenler;
    public String tip; // "TETIKLEYICI" veya "-"
}