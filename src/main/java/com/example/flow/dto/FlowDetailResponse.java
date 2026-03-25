package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlowDetailResponse {

    private Long flowId;
    private String flowName;
    private String aciklama;
    private List<StepDetailResponse> steps;
}