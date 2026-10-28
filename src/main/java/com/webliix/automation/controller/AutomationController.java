package com.webliix.automation.controller;

import com.webliix.automation.dto.*;
import com.webliix.automation.enums.TriggerType;
import com.webliix.automation.service.AutomationService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/automation")
@RequirePermission(anyOf = {"AUTOMATION_MANAGE", "SETTINGS_MANAGE", "REPORTS_VIEW", "SUPER_ADMIN", "ADMIN"})
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

    @DeleteMapping("/executions")
    public ResponseEntity<ApiResponse<String>> clearExecutions() {
        automationService.clearExecutions();
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Automation execution logs cleared")
                .data("OK")
                .build());
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

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<ScheduledJobResponse>>> getScheduledJobs() {
        List<ScheduledJobResponse> jobs = automationService.getScheduledJobs();
        return ResponseEntity.ok(ApiResponse.<List<ScheduledJobResponse>>builder()
                .success(true)
                .message("Scheduled automation jobs fetched")
                .data(jobs)
                .build());
    }

    @PostMapping("/jobs/{jobKey}/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runScheduledJob(@PathVariable String jobKey) {
        Map<String, Object> result = automationService.runScheduledJobNow(jobKey);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(Boolean.TRUE.equals(result.get("success")))
                .message(String.valueOf(result.get("message")))
                .data(result)
                .build());
    }

    @PostMapping("/jobs/{jobKey}/toggle")
    public ResponseEntity<ApiResponse<ScheduledJobResponse>> toggleScheduledJob(@PathVariable String jobKey) {
        ScheduledJobResponse response = automationService.toggleScheduledJob(jobKey);
        return ResponseEntity.ok(ApiResponse.<ScheduledJobResponse>builder()
                .success(true)
                .message("Scheduled job state updated")
                .data(response)
                .build());
    }
}
