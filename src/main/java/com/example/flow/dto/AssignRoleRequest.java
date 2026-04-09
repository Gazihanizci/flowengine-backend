package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {

    private Long kullaniciId;
    private Long rolId;
}