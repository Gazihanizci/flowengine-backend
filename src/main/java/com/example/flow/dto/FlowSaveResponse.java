package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FlowSaveResponse {
    private Long flowId;
    private String message;
}