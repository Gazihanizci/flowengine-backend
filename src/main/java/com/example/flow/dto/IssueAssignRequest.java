package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueAssignRequest {

    private Long userId;
    private Long roleId;
}