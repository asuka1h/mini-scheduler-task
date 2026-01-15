package com.minischeduler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskLogController {
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{taskId}/logs")
    public ResponseEntity<Void> receiveTaskLog(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> logMessage) {
        String log = (String) logMessage.get("log");
        
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("taskId", taskId);
        wsMessage.put("log", log);
        wsMessage.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/task-logs/" + taskId, wsMessage);
        return ResponseEntity.ok().build();
    }
}
