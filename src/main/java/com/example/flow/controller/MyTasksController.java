package com.example.flow.controller;

import com.example.flow.dto.TaskResponse;
import com.example.flow.security.CurrentUser;
import com.example.flow.service.MyTasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mytasks")
@RequiredArgsConstructor
public class MyTasksController {
    private final CurrentUser currentUser;
    private final MyTasksService myTasksService;

    @GetMapping
    public List<TaskResponse> getMyTasks() {

        Long userId = currentUser.id(); // 🔥 JWT'den user al

        return myTasksService.getMyTasks(userId);
    }
}