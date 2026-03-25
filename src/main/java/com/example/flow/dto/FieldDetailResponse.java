package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldDetailResponse {

    private Long fieldId;
    private String type;
    private String label;
    private String placeholder;
    private Boolean required;
    private Integer orderNo;
    private List<OptionDetailResponse> options;
}