package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("UPDATE_STATUS")
public class UpdateStatusAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String entity = String.valueOf(parameters.getOrDefault("entity", "unknown"));
        String status = String.valueOf(parameters.getOrDefault("status", "UPDATED"));
        logger.info("Automation UPDATE_STATUS executed: entity={}, status={}", entity, status);
    }
}
