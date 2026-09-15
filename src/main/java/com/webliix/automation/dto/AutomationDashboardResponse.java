package com.webliix.automation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AutomationDashboardResponse {
    private long totalRules;
    private long activeRules;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    private Map<String, Long> executionsByTrigger;
}
