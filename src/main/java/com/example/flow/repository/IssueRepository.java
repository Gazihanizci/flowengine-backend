package com.example.flow.repository;

import com.example.flow.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByAssignedUserId(Long userId);

    List<Issue> findByCreatedBy(Long userId);
}