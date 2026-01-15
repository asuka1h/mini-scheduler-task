package com.minischeduler.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskSubmitRequest {
    @NotBlank(message = "Command is required")
    private String command;

    @NotNull(message = "CPU required is required")
    @Min(value = 1, message = "CPU required must be at least 1")
    private Integer cpuRequired;

    @NotNull(message = "Memory required is required")
    @Min(value = 1, message = "Memory required must be at least 1 MB")
    private Integer memRequired;
}
