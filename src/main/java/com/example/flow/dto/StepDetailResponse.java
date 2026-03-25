package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StepDetailResponse {

    private Long stepId;
    private String stepName;
    private Integer stepOrder;
    private List<FieldDetailResponse> fields;
}