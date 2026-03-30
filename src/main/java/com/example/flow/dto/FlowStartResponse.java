package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FlowStartResponse {

    private Long surecId;
    private Long mevcutAdimId;
    private String mesaj;
}