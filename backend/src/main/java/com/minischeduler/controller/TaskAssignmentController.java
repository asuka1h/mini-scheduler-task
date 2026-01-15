package com.minischeduler.controller;

import com.minischeduler.model.Task;
import com.minischeduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class TaskAssignmentController {
    private final SchedulerService schedulerService;

    @GetMapping("/{workerId}/tasks")
    public ResponseEntity<Collection<Task>> getAssignedTasks(@PathVariable String workerId) {
        Collection<Task> tasks = schedulerService.getAllTasks().stream()
                .filter(t -> workerId.equals(t.getWorkerId()) && 
                            t.getStatus() == Task.TaskStatus.RUNNING)
                .toList();
        return ResponseEntity.ok(tasks);
    }
}
