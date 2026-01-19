package com.minischeduler.worker;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Data
@Component
public class WorkerNode {
    @Value("${worker.host:localhost}")
    private String host;

    @Value("${worker.port:8081}")
    private int port;

    @Value("${worker.master.url:http://localhost:8080}")
    private String masterUrl;

    private String id;
    private int totalCpu;
    private int totalMemory; // MB

    public void initialize() {
        this.id = java.util.UUID.randomUUID().toString();
        
        // 获取系统CPU核心数
        this.totalCpu = Runtime.getRuntime().availableProcessors();
        
        // 获取系统总内存（MB）
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.totalMemory = (int) (osBean.getTotalMemorySize() / (1024 * 1024));
    }
}
