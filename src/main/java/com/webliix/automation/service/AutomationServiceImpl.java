package com.webliix.automation.service;

import com.webliix.automation.dto.*;
import com.webliix.automation.entity.AutomationExecution;
import com.webliix.automation.entity.AutomationRule;
import com.webliix.automation.engine.AutomationEngine;
import com.webliix.automation.enums.AutomationExecutionStatus;
import com.webliix.automation.enums.TriggerType;
import com.webliix.automation.mapper.AutomationRuleMapper;
import com.webliix.automation.repository.AutomationExecutionRepository;
import com.webliix.automation.repository.AutomationRuleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AutomationServiceImpl implements AutomationService {

    private final AutomationRuleRepository ruleRepository;
    private final AutomationExecutionRepository executionRepository;
    private final AutomationEngine automationEngine;

    public AutomationServiceImpl(
            AutomationRuleRepository ruleRepository,
            AutomationExecutionRepository executionRepository,
            AutomationEngine automationEngine) {
        this.ruleRepository = ruleRepository;
        this.executionRepository = executionRepository;
        this.automationEngine = automationEngine;
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public AutomationRuleResponse createRule(CreateAutomationRuleRequest request) {
        AutomationRule rule = AutomationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .conditionJson(request.getConditionJson())
                .actionJson(request.getActionJson())
                .enabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return AutomationRuleMapper.toResponse(ruleRepository.save(rule));
    }

    @Override
    public List<AutomationRuleResponse> getRules() {
        return ruleRepository.findAll().stream()
                .map(AutomationRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public AutomationRuleResponse updateRule(Long id, UpdateAutomationRuleRequest request) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found: " + id));
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTriggerType(request.getTriggerType());
        rule.setConditionJson(request.getConditionJson());
        rule.setActionJson(request.getActionJson());
        rule.setEnabled(request.getEnabled() == null ? rule.getEnabled() : request.getEnabled());
        rule.setUpdatedAt(LocalDateTime.now());
        return AutomationRuleMapper.toResponse(ruleRepository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Override
    public List<AutomationExecutionResponse> getExecutions() {
        return executionRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toExecutionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "automationDashboard")
    public AutomationDashboardResponse getDashboard() {
        long totalRules = ruleRepository.count();
        long activeRules = ruleRepository.countByEnabledTrue();
        long totalExecutions = executionRepository.count();
        long successfulExecutions = executionRepository.countByStatus(AutomationExecutionStatus.SUCCESS);
        long failedExecutions = executionRepository.countByStatus(AutomationExecutionStatus.FAILED);
        Map<String, Long> executionsByTrigger = ruleRepository.countGroupByTriggerType().stream()
                .collect(Collectors.toMap(
                        row -> ((TriggerType) row[0]).name(),
                        row -> ((Number) row[1]).longValue()
                ));

        return AutomationDashboardResponse.builder()
                .totalRules(totalRules)
                .activeRules(activeRules)
                .totalExecutions(totalExecutions)
                .successfulExecutions(successfulExecutions)
                .failedExecutions(failedExecutions)
                .executionsByTrigger(executionsByTrigger)
                .build();
    }

    @Override
    public AutomationExecutionResponse executeTrigger(TriggerType triggerType, Map<String, Object> context) {
        AutomationExecution execution = automationEngine.executeTrigger(triggerType, context);
        return toExecutionResponse(execution);
    }

    private AutomationExecutionResponse toExecutionResponse(AutomationExecution execution) {
        return AutomationExecutionResponse.builder()
                .id(execution.getId())
                .ruleId(execution.getRuleId())
                .status(execution.getStatus())
                .executionTime(execution.getExecutionTime())
                .errorMessage(execution.getErrorMessage())
                .createdAt(execution.getCreatedAt())
                .build();
    }
}
