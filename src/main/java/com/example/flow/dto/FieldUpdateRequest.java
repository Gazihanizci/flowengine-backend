package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldUpdateRequest {

    private String label;
    private String placeholder;
    private Boolean required;

    private List<PermissionDto> permissions;
    private List<OptionSaveRequest> options;
}