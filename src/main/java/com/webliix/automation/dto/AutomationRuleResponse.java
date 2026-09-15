package com.webliix.automation.dto;

import com.webliix.automation.enums.TriggerType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AutomationRuleResponse {
    private Long id;
    private String name;
    private String description;
    private TriggerType triggerType;
    private String conditionJson;
    private String actionJson;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
