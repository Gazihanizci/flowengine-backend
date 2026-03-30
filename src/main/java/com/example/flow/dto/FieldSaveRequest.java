package com.example.flow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldSaveRequest {

    @NotBlank(message = "Field type zorunludur")
    private String type;

    @NotBlank(message = "Field label zorunludur")
    private String label;

    private String placeholder;

    private Boolean required;

    private Integer orderNo;

    // 🔥 YENİ (çoklu destek)
    private List<Long> roleIds;
    private List<Long> userIds;

    @Valid
    private List<OptionSaveRequest> options;
}