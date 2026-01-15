package com.minischeduler.worker;

import com.minischeduler.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WorkerService {
    private final WorkerNode workerNode;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, Process> runningProcesses = new HashMap<>();

    public void registerWithMaster() {
        try {
            Map<String, Object> workerInfo = new HashMap<>();
            workerInfo.put("id", workerNode.getId());
            workerInfo.put("host", workerNode.getHost());
            workerInfo.put("port", workerNode.getPort());
            workerInfo.put("totalCpu", workerNode.getTotalCpu());
            workerInfo.put("totalMemory", workerNode.getTotalMemory());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(workerInfo, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    workerNode.getMasterUrl() + "/api/workers/register",
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Worker registered successfully with ID: " + workerNode.getId());
            }
        } catch (Exception e) {
            System.err.println("Failed to register with master: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3000) // 每3秒发送一次心跳并检查新任务
    public void sendHeartbeat() {
        try {
            restTemplate.postForEntity(
                    workerNode.getMasterUrl() + "/api/workers/" + workerNode.getId() + "/heartbeat",
                    null,
                    Void.class
            );
            
            // 检查是否有分配给自己的新任务
            checkAndExecuteTasks();
        } catch (Exception e) {
            System.err.println("Failed to send heartbeat: " + e.getMessage());
        }
    }

    private void checkAndExecuteTasks() {
        try {
            ResponseEntity<Collection<Task>> response = restTemplate.exchange(
                    workerNode.getMasterUrl() + "/api/workers/" + workerNode.getId() + "/tasks",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Collection<Task>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                for (Task task : response.getBody()) {
                    if (!runningProcesses.containsKey(task.getId())) {
                        executeTask(task);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to check tasks: " + e.getMessage());
        }
    }

    public void executeTask(Task task) {
        CompletableFuture.runAsync(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("sh", "-c", task.getCommand());
                processBuilder.redirectErrorStream(true);
                
                Process process = processBuilder.start();
                runningProcesses.put(task.getId(), process);

                // 读取日志并发送到WebSocket
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sendTaskLog(task.getId(), line);
                    }
                }

                int exitCode = process.waitFor();
                runningProcesses.remove(task.getId());

                // 通知Master任务完成
                notifyTaskCompletion(task.getId(), exitCode == 0, null);

            } catch (Exception e) {
                runningProcesses.remove(task.getId());
                notifyTaskCompletion(task.getId(), false, e.getMessage());
            }
        });
    }

    private void sendTaskLog(String taskId, String logLine) {
        try {
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("taskId", taskId);
            logMessage.put("log", logLine);
            logMessage.put("timestamp", System.currentTimeMillis());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(logMessage, headers);

            restTemplate.postForEntity(
                    workerNode.getMasterUrl() + "/api/tasks/" + taskId + "/logs",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Failed to send task log: " + e.getMessage());
        }
    }

    private void notifyTaskCompletion(String taskId, boolean success, String errorMessage) {
        try {
            Map<String, Object> completion = new HashMap<>();
            completion.put("taskId", taskId);
            completion.put("success", success);
            completion.put("errorMessage", errorMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(completion, headers);

            restTemplate.postForEntity(
                    workerNode.getMasterUrl() + "/api/tasks/" + taskId + "/complete",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Failed to notify task completion: " + e.getMessage());
        }
    }
}
