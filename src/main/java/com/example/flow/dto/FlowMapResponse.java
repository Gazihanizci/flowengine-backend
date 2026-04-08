package com.example.flow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FlowMapResponse {
    private Long akisId;
    private String akisAdi;
    private List<StepNodeDTO> adimlar;
}