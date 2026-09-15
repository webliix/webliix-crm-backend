package com.webliix.automation.dto;

import com.webliix.automation.enums.TriggerType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAutomationRuleRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private TriggerType triggerType;

    private String conditionJson;

    @NotBlank
    private String actionJson;

    private Boolean enabled = true;
}
