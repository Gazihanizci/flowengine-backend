package com.example.flow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCreateRequest {

    private String title;
    private String description;
    private String priority;
    private Long assignedUserId;
    private Long akisId;
}