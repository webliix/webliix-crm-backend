package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("ASSIGN_EMPLOYEE")
public class AssignEmployeeAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(AssignEmployeeAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String employee = String.valueOf(parameters.getOrDefault("employee", "unassigned"));
        String task = String.valueOf(parameters.getOrDefault("task", "general"));
        logger.info("Automation ASSIGN_EMPLOYEE executed: employee={}, task={}", employee, task);
    }
}
