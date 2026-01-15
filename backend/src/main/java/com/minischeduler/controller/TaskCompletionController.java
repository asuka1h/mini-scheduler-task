package com.minischeduler.controller;

import com.minischeduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskCompletionController {
    private final SchedulerService schedulerService;

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> completion) {
        boolean success = (Boolean) completion.getOrDefault("success", false);
        String errorMessage = (String) completion.get("errorMessage");
        schedulerService.completeTask(taskId, success, errorMessage);
        return ResponseEntity.ok().build();
    }
}
