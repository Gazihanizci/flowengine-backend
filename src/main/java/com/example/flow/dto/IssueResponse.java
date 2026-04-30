package com.example.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class IssueResponse {

    private Long id;
    private String title;
    private String description;
    private String priority;
    private Long createdBy;
    private Long assignedUserId;
    private Long akisId;
    private String status;
    private LocalDateTime createdAt;
}