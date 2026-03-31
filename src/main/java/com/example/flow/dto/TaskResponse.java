package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskResponse {

    private Long taskId;
    private Long surecId;
    private Long adimId;
    private String adimAdi;

    private List<FieldResponse> form;
}