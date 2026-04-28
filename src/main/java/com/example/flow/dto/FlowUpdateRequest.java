package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlowUpdateRequest {

    private String flowName;
    private String aciklama;

    private List<BaslatmaYetkiDto> baslatmaYetkileri;
}