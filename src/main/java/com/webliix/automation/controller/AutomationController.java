package com.webliix.automation.controller;

import com.webliix.automation.dto.*;
import com.webliix.automation.enums.TriggerType;
import com.webliix.automation.service.AutomationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @PostMapping("/rules")
    public ResponseEntity<AutomationRuleResponse> createRule(@Valid @RequestBody CreateAutomationRuleRequest request) {
        return ResponseEntity.ok(automationService.createRule(request));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AutomationRuleResponse>> getRules() {
        return ResponseEntity.ok(automationService.getRules());
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<AutomationRuleResponse> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAutomationRuleRequest request) {
        return ResponseEntity.ok(automationService.updateRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        automationService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/executions")
    public ResponseEntity<List<AutomationExecutionResponse>> getExecutions() {
        return ResponseEntity.ok(automationService.getExecutions());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AutomationDashboardResponse> getDashboard() {
        return ResponseEntity.ok(automationService.getDashboard());
    }

    @PostMapping("/triggers/{triggerType}/execute")
    public ResponseEntity<AutomationExecutionResponse> executeTrigger(
            @PathVariable TriggerType triggerType,
            @RequestBody(required = false) Map<String, Object> context) {
        return ResponseEntity.ok(automationService.executeTrigger(triggerType, context == null ? Map.of() : context));
    }
}
