package com.minischeduler.controller;

import com.minischeduler.model.Task;
import com.minischeduler.model.WorkerNode;
import com.minischeduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SchedulerService schedulerService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 1000) // 每秒推送一次集群状态
    public void broadcastClusterStatus() {
        Collection<WorkerNode> workers = schedulerService.getAllWorkers();
        
        // 检查worker心跳，超过5秒未收到心跳则标记为离线
        LocalDateTime now = LocalDateTime.now();
        workers.forEach(worker -> {
            if (worker.getLastHeartbeat() != null && 
                worker.getLastHeartbeat().plusSeconds(5).isBefore(now)) {
                worker.setOnline(false);
            }
        });

        Map<String, Object> status = new HashMap<>();
        status.put("workers", workers);
        status.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/cluster-status", status);
    }

    public void sendTaskLog(String taskId, String logLine) {
        Map<String, Object> logMessage = new HashMap<>();
        logMessage.put("taskId", taskId);
        logMessage.put("log", logLine);
        logMessage.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/task-logs/" + taskId, logMessage);
    }
}
