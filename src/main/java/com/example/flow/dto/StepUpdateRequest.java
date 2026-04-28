package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StepUpdateRequest {

    private String stepName;
    private Integer stepOrder;
    private Integer requiredApprovalCount;

    private Boolean externalFlowEnabled;
    private Long externalFlowId;

    private Boolean waitForExternalFlowCompletion;
    private Boolean resumeParentAfterSubFlow;

    private String cancelBehavior;
}
