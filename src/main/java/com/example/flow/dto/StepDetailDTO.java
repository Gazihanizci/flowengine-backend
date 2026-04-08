package com.example.flow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StepDetailDTO {
    private Long adimId;
    private String adimAdi;
    private Integer sira;
    private boolean altAkisTetikler_Mi;
    private Long altAkisId;
    private List<ComponentInfoDTO> bilesenler;
    private List<Long> sonrakiAdimlar;
}