package com.webliix.automation.dto;

import com.webliix.automation.enums.AutomationExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AutomationExecutionResponse {
    private Long id;
    private Long ruleId;
    private AutomationExecutionStatus status;
    private LocalDateTime executionTime;
    private String errorMessage;
    private LocalDateTime createdAt;
}
