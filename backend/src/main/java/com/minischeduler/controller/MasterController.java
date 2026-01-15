package com.minischeduler.controller;

import com.minischeduler.model.Task;
import com.minischeduler.model.TaskSubmitRequest;
import com.minischeduler.model.WorkerNode;
import com.minischeduler.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterController {
    private final SchedulerService schedulerService;

    @PostMapping("/workers/register")
    public ResponseEntity<WorkerNode> registerWorker(@RequestBody WorkerNode worker) {
        if (worker.getId() == null) {
            worker.setId(UUID.randomUUID().toString());
        }
        worker.setOnline(true);
        worker.setLastHeartbeat(LocalDateTime.now());
        schedulerService.registerWorker(worker);
        return ResponseEntity.ok(worker);
    }

    @PostMapping("/workers/{workerId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable String workerId) {
        schedulerService.updateWorkerHeartbeat(workerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> submitTask(@Valid @RequestBody TaskSubmitRequest request) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setCommand(request.getCommand());
        task.setCpuRequired(request.getCpuRequired());
        task.setMemRequired(request.getMemRequired());
        task.setStatus(Task.TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());

        Task submittedTask = schedulerService.submitTask(task);
        return ResponseEntity.ok(submittedTask);
    }

    @GetMapping("/tasks")
    public ResponseEntity<Collection<Task>> getAllTasks() {
        return ResponseEntity.ok(schedulerService.getAllTasks());
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable String taskId) {
        Task task = schedulerService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/workers")
    public ResponseEntity<Collection<WorkerNode>> getAllWorkers() {
        return ResponseEntity.ok(schedulerService.getAllWorkers());
    }
}
