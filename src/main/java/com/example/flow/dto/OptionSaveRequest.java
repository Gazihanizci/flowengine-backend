package com.example.flow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionSaveRequest {

    @NotBlank(message = "Option label zorunludur")
    private String label;

    @NotBlank(message = "Option value zorunludur")
    private String value;
}