package com.example.flow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StepSaveRequest {

    @NotBlank(message = "Step adı zorunludur")
    private String stepName;

    @NotNull(message = "Step sırası zorunludur")
    private Integer stepOrder;

    @Valid
    private List<FieldSaveRequest> fields;
    private Boolean externalFlowEnabled;

    private Long externalFlowId;
    private Long subFlowId;
    private Long nextFlowId;

    private Boolean waitForExternalFlowCompletion;
    private Boolean resumeParentAfterSubFlow;

    private String cancelBehavior;
    private Integer requiredApprovalCount;
}