package com.minischeduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerNode {
    private String id;
    private String host;
    private int port;
    private int totalCpu;
    private int totalMemory; // MB
    private int usedCpu;
    private int usedMemory; // MB
    private LocalDateTime lastHeartbeat;
    private boolean online;
    private List<String> runningTasks = new CopyOnWriteArrayList<>();

    public int getAvailableCpu() {
        return totalCpu - usedCpu;
    }

    public int getAvailableMemory() {
        return totalMemory - usedMemory;
    }

    public boolean canAllocate(int cpuRequired, int memRequired) {
        return getAvailableCpu() >= cpuRequired && getAvailableMemory() >= memRequired;
    }

    public void allocate(int cpuRequired, int memRequired, String taskId) {
        usedCpu += cpuRequired;
        usedMemory += memRequired;
        runningTasks.add(taskId);
    }

    public void release(int cpuRequired, int memRequired, String taskId) {
        usedCpu -= cpuRequired;
        usedMemory -= memRequired;
        runningTasks.remove(taskId);
    }
}
