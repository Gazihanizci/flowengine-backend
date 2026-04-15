package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDto {

    private String tip;
    private Long refId;
    private String yetkiTipi;// VIEW / EDIT
}