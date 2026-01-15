package com.minischeduler.service;

import com.minischeduler.model.Task;
import com.minischeduler.model.WorkerNode;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SchedulerService {
    private final Map<String, WorkerNode> workers = new ConcurrentHashMap<>();
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    public void registerWorker(WorkerNode worker) {
        workers.put(worker.getId(), worker);
    }

    public void updateWorkerHeartbeat(String workerId) {
        WorkerNode worker = workers.get(workerId);
        if (worker != null) {
            worker.setLastHeartbeat(java.time.LocalDateTime.now());
            worker.setOnline(true);
        }
    }

    public void markWorkerOffline(String workerId) {
        WorkerNode worker = workers.get(workerId);
        if (worker != null) {
            worker.setOnline(false);
        }
    }

    public WorkerNode findBestWorker(int cpuRequired, int memRequired) {
        return workers.values().stream()
                .filter(WorkerNode::isOnline)
                .filter(w -> w.canAllocate(cpuRequired, memRequired))
                .min(Comparator
                        .comparing((WorkerNode w) -> w.getAvailableCpu() - cpuRequired)
                        .thenComparing(w -> w.getAvailableMemory() - memRequired))
                .orElse(null);
    }

    public Task submitTask(Task task) {
        tasks.put(task.getId(), task);
        scheduleTask(task);
        return task;
    }

    private void scheduleTask(Task task) {
        WorkerNode worker = findBestWorker(task.getCpuRequired(), task.getMemRequired());
        if (worker != null) {
            worker.allocate(task.getCpuRequired(), task.getMemRequired(), task.getId());
            task.setWorkerId(worker.getId());
            task.setStatus(Task.TaskStatus.RUNNING);
            task.setStartedAt(java.time.LocalDateTime.now());
        } else {
            task.setStatus(Task.TaskStatus.PENDING);
        }
    }

    public void completeTask(String taskId, boolean success, String errorMessage) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(success ? Task.TaskStatus.SUCCESS : Task.TaskStatus.FAILED);
            task.setFinishedAt(java.time.LocalDateTime.now());
            if (errorMessage != null) {
                task.setErrorMessage(errorMessage);
            }

            if (task.getWorkerId() != null) {
                WorkerNode worker = workers.get(task.getWorkerId());
                if (worker != null) {
                    worker.release(task.getCpuRequired(), task.getMemRequired(), taskId);
                }
            }

            // 尝试调度pending的任务
            reschedulePendingTasks();
        }
    }

    private void reschedulePendingTasks() {
        tasks.values().stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.PENDING)
                .forEach(this::scheduleTask);
    }

    public Collection<WorkerNode> getAllWorkers() {
        return workers.values();
    }

    public Collection<Task> getAllTasks() {
        return tasks.values();
    }

    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }
}
