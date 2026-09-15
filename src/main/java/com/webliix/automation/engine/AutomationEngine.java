package com.webliix.automation.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.automation.action.AutomationAction;
import com.webliix.automation.condition.ConditionEvaluator;
import com.webliix.automation.entity.AutomationExecution;
import com.webliix.automation.entity.AutomationRule;
import com.webliix.automation.enums.AutomationExecutionStatus;
import com.webliix.automation.enums.TriggerType;
import com.webliix.automation.repository.AutomationExecutionRepository;
import com.webliix.automation.repository.AutomationRuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AutomationEngine {

    private static final Logger logger = LoggerFactory.getLogger(AutomationEngine.class);

    private final AutomationRuleRepository ruleRepository;
    private final AutomationExecutionRepository executionRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final ObjectMapper objectMapper;
    private final Map<String, AutomationAction> actionRegistry;

    public AutomationEngine(
            AutomationRuleRepository ruleRepository,
            AutomationExecutionRepository executionRepository,
            ConditionEvaluator conditionEvaluator,
            ObjectMapper objectMapper,
            Map<String, AutomationAction> actionRegistry) {
        this.ruleRepository = ruleRepository;
        this.executionRepository = executionRepository;
        this.conditionEvaluator = conditionEvaluator;
        this.objectMapper = objectMapper;
        this.actionRegistry = actionRegistry;
    }

    @Transactional
    public AutomationExecution executeTrigger(TriggerType triggerType, Map<String, Object> context) {
        List<AutomationRule> rules = ruleRepository.findByTriggerTypeAndEnabledTrue(triggerType);
        AutomationExecution lastExecution = null;

        for (AutomationRule rule : rules) {
            AutomationExecution execution = AutomationExecution.builder()
                    .ruleId(rule.getId())
                    .status(AutomationExecutionStatus.RUNNING)
                    .executionTime(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            execution = executionRepository.save(execution);

            try {
                if (conditionEvaluator.evaluate(rule.getConditionJson(), context)) {
                    executeRuleAction(rule, context);
                }
                execution.setStatus(AutomationExecutionStatus.SUCCESS);
                execution.setErrorMessage(null);
            } catch (Exception ex) {
                logger.error("Automation rule execution failed for rule {}", rule.getId(), ex);
                execution.setStatus(AutomationExecutionStatus.FAILED);
                execution.setErrorMessage(ex.getMessage());
            }
            execution = executionRepository.save(execution);
            lastExecution = execution;
        }

        if (lastExecution == null) {
            AutomationExecution noRuleExecution = AutomationExecution.builder()
                    .ruleId(null)
                    .status(AutomationExecutionStatus.SUCCESS)
                    .executionTime(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            return executionRepository.save(noRuleExecution);
        }

        return lastExecution;
    }

    private void executeRuleAction(AutomationRule rule, Map<String, Object> context) {
        String actionJson = rule.getActionJson();
        if (actionJson == null || actionJson.isBlank()) {
            logger.warn("Skipping automation rule {} because actionJson is empty", rule.getId());
            return;
        }

        try {
            JsonNode actionNode = objectMapper.readTree(actionJson);
            String actionTypeValue = actionNode.has("actionType") ? actionNode.get("actionType").asText() : null;
            JsonNode paramsNode = actionNode.has("params") ? actionNode.get("params") : objectMapper.createObjectNode();
            if (actionTypeValue == null || actionTypeValue.isBlank()) {
                logger.warn("Automation rule {} contains no actionType", rule.getId());
                return;
            }

            AutomationAction action = actionRegistry.get(actionTypeValue);
            if (action == null) {
                logger.warn("No automation action registered for type {}", actionTypeValue);
                return;
            }

            Map<String, Object> params = objectMapper.convertValue(paramsNode, new TypeReference<>() {});
            action.execute(context, params);
        } catch (com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to execute action for rule " + rule.getId(), ex);
        }
    }
}
