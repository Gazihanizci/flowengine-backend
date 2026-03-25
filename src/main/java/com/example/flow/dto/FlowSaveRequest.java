package com.example.flow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlowSaveRequest {

    @NotBlank(message = "Flow adı zorunludur")
    private String flowName;

    private String aciklama;

    @Valid
    @NotEmpty(message = "En az 1 step olmalıdır")
    private List<StepSaveRequest> steps;
}