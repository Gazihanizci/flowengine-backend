package com.example.flow.controller;

import com.example.flow.dto.TaskActionRequest;
import com.example.flow.service.TaskActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskActionService taskActionService;

    @PostMapping("/{taskId}/action")
    public void action(
            @PathVariable Long taskId,
            @RequestBody TaskActionRequest request
    ) {
        taskActionService.handleAction(
                taskId,
                request.getAksiyonId(),
                request.getFormData()
        );
    }
}