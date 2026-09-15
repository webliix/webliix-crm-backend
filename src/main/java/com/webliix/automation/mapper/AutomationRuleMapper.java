package com.webliix.automation.mapper;

import com.webliix.automation.dto.AutomationRuleResponse;
import com.webliix.automation.entity.AutomationRule;

public class AutomationRuleMapper {

    public static AutomationRuleResponse toResponse(AutomationRule rule) {
        return AutomationRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .triggerType(rule.getTriggerType())
                .conditionJson(rule.getConditionJson())
                .actionJson(rule.getActionJson())
                .enabled(rule.getEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
