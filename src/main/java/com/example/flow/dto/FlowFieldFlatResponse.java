package com.example.flow.dto;

public interface FlowFieldFlatResponse {

    Long getAkisId();
    String getAkisAdi();

    Long getAdimId();
    String getAdimAdi();

    Long getBilesenId();
    String getBilesenTipi();
    String getLabel();
    String getPlaceholder();
    Boolean getZorunlu();
    Integer getSiraNo();
    String getAciklama();
    String getOptionLabel();
    String getOptionValue();
}