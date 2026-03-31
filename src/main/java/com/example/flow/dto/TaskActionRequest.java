package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TaskActionRequest {

    private Long aksiyonId;

    // key = bilesenId , value = girilen değer
    private Map<Long, String> formData;
}