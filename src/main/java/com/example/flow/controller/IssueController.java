package com.example.flow.controller;

import com.example.flow.dto.*;
import com.example.flow.entity.IssueComment;
import com.example.flow.entity.IssueStatusHistory;
import com.example.flow.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    public IssueResponse create(@RequestBody IssueCreateRequest request) {
        return issueService.create(request);
    }

    @GetMapping
    public List<IssueResponse> getAll() {
        return issueService.getAll();
    }

    @GetMapping("/{issueId}")
    public IssueResponse getById(@PathVariable Long issueId) {
        return issueService.getById(issueId);
    }

    @GetMapping("/my")
    public List<IssueResponse> getMyIssues() {
        return issueService.getMyAssignedIssues();
    }

    @PutMapping("/{issueId}/status")
    public IssueResponse updateStatus(
            @PathVariable Long issueId,
            @RequestBody IssueStatusUpdateRequest request
    ) {
        return issueService.updateStatus(issueId, request);
    }

    @PutMapping("/{issueId}/assign")
    public IssueResponse assign(
            @PathVariable Long issueId,
            @RequestBody IssueAssignRequest request
    ) {
        return issueService.assign(issueId, request);
    }

    @PostMapping("/{issueId}/comments")
    public IssueComment addComment(
            @PathVariable Long issueId,
            @RequestBody IssueCommentRequest request
    ) {
        return issueService.addComment(issueId, request);
    }

    @GetMapping("/{issueId}/comments")
    public List<IssueComment> getComments(@PathVariable Long issueId) {
        return issueService.getComments(issueId);
    }

    @GetMapping("/{issueId}/history")
    public List<IssueStatusHistory> getHistory(@PathVariable Long issueId) {
        return issueService.getHistory(issueId);
    }
}