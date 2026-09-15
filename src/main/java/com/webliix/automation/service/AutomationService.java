package com.webliix.automation.service;

import com.webliix.automation.dto.*;
import com.webliix.automation.enums.TriggerType;

import java.util.List;
import java.util.Map;

public interface AutomationService {

    AutomationRuleResponse createRule(CreateAutomationRuleRequest request);

    List<AutomationRuleResponse> getRules();

    AutomationRuleResponse updateRule(Long id, UpdateAutomationRuleRequest request);

    void deleteRule(Long id);

    List<AutomationExecutionResponse> getExecutions();

    AutomationDashboardResponse getDashboard();

    AutomationExecutionResponse executeTrigger(TriggerType triggerType, Map<String, Object> context);
}
