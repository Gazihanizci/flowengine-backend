package com.example.flow.service;

import com.example.flow.dto.*;
import com.example.flow.entity.*;
import com.example.flow.repository.*;
import com.example.flow.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final StatusRepository statusRepository;
    private final IssueStatusHistoryRepository historyRepository;
    private final IssueCommentRepository commentRepository;
    private final IssueAssignmentRepository assignmentRepository;
    private final CurrentUser currentUser;

    @Transactional
    public IssueResponse create(IssueCreateRequest request) {

        Long userId = currentUser.id();

        Status defaultStatus = statusRepository.findByName("TODO")
                .orElseThrow(() -> new RuntimeException("TODO status bulunamadı"));

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setPriority(request.getPriority());
        issue.setAssignedUserId(request.getAssignedUserId());
        issue.setAkisId(request.getAkisId());
        issue.setCreatedBy(userId);
        issue.setStatus(defaultStatus);
        issue.setCreatedAt(LocalDateTime.now());

        Issue saved = issueRepository.save(issue);

        saveHistory(saved.getId(), defaultStatus.getId(), userId);

        if (request.getAssignedUserId() != null) {
            IssueAssignment assignment = new IssueAssignment();
            assignment.setIssueId(saved.getId());
            assignment.setUserId(request.getAssignedUserId());
            assignment.setAssignedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
        }

        return toResponse(saved);
    }

    public List<IssueResponse> getAll() {
        return issueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IssueResponse getById(Long issueId) {
        Issue issue = findIssue(issueId);
        return toResponse(issue);
    }

    public List<IssueResponse> getMyAssignedIssues() {
        Long userId = currentUser.id();

        return issueRepository.findByAssignedUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse updateStatus(Long issueId, IssueStatusUpdateRequest request) {

        Long userId = currentUser.id();

        Issue issue = findIssue(issueId);

        Status status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status bulunamadı"));

        issue.setStatus(status);

        Issue saved = issueRepository.save(issue);

        saveHistory(issueId, status.getId(), userId);

        return toResponse(saved);
    }

    @Transactional
    public IssueResponse assign(Long issueId, IssueAssignRequest request) {

        Issue issue = findIssue(issueId);

        issue.setAssignedUserId(request.getUserId());

        Issue saved = issueRepository.save(issue);

        IssueAssignment assignment = new IssueAssignment();
        assignment.setIssueId(issueId);
        assignment.setUserId(request.getUserId());
        assignment.setRoleId(request.getRoleId());
        assignment.setAssignedAt(LocalDateTime.now());

        assignmentRepository.save(assignment);

        return toResponse(saved);
    }

    @Transactional
    public IssueComment addComment(Long issueId, IssueCommentRequest request) {

        Long userId = currentUser.id();

        findIssue(issueId);

        IssueComment comment = new IssueComment();
        comment.setIssueId(issueId);
        comment.setUserId(userId);
        comment.setMessage(request.getMessage());
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<IssueComment> getComments(Long issueId) {
        findIssue(issueId);
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }

    public List<IssueStatusHistory> getHistory(Long issueId) {
        findIssue(issueId);
        return historyRepository.findByIssueIdOrderByChangedAtDesc(issueId);
    }

    private Issue findIssue(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue bulunamadı"));
    }

    private void saveHistory(Long issueId, Long statusId, Long userId) {
        IssueStatusHistory history = new IssueStatusHistory();
        history.setIssueId(issueId);
        history.setStatusId(statusId);
        history.setChangedBy(userId);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getPriority(),
                issue.getCreatedBy(),
                issue.getAssignedUserId(),
                issue.getAkisId(),
                issue.getStatus() != null ? issue.getStatus().getName() : null,
                issue.getCreatedAt()
        );
    }
}