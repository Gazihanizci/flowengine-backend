package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldCreateRequest {

    private String label;
    private String placeholder;
    private Boolean required;
    private String type; // TEXT, FILE, SELECT

    private List<PermissionDto> permissions;
    private List<OptionSaveRequest> options;
}