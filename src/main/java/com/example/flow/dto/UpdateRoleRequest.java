package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {

    private Long kullaniciId;
    private Long eskiRolId;
    private Long yeniRolId;
}