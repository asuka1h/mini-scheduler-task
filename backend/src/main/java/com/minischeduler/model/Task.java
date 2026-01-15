package com.minischeduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    public enum TaskStatus {
        PENDING, RUNNING, SUCCESS, FAILED
    }

    private String id;
    private String command;
    private int cpuRequired;
    private int memRequired; // MB
    private TaskStatus status;
    private String workerId;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String errorMessage;
}
