package com.example.flow.controller;

import com.example.flow.dto.TaskResponse;
import com.example.flow.security.CurrentUser;
import com.example.flow.service.FlowDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flow-detail")
@RequiredArgsConstructor
public class FlowDetailController {

    private final FlowDetailService flowDetailService;
    private final CurrentUser currentUser;

    @GetMapping("/{surecId}")
    public List<TaskResponse> getFlowDetail(@PathVariable Long surecId) {
        return flowDetailService.getFlowDetail(surecId, currentUser.id());
    }
}