package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldResponse {
    private Long fileId;
    private Long fieldId;
    private String type;
    private String label;

    private boolean editable;
    private String value;
    private List<OptionResponse> options;
}