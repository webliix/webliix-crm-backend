package com.webliix.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledJobResponse {
    private String jobKey;
    private String name;
    private String description;
    private String schedule;
    private String cronExpression;
    private String category;
    private boolean enabled;
    private LocalDateTime lastRunAt;
    private String lastRunStatus;
    private String lastRunSummary;
}
