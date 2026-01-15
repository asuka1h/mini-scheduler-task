package com.minischeduler.config;

import com.minischeduler.worker.WorkerNode;
import com.minischeduler.worker.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class AppConfig {
    private final WorkerNode workerNode;
    private final WorkerService workerService;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @PostConstruct
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "worker")
    public void initWorker() {
        workerNode.initialize();
        workerService.registerWithMaster();
    }
}
