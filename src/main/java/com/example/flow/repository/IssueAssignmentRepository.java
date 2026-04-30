package com.example.flow.repository;

import com.example.flow.entity.IssueAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueAssignmentRepository extends JpaRepository<IssueAssignment, Long> {

    List<IssueAssignment> findByIssueId(Long issueId);
}