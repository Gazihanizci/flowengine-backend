package com.example.flow.repository;

import com.example.flow.entity.IssueStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {

    List<IssueStatusHistory> findByIssueIdOrderByChangedAtDesc(Long issueId);
}