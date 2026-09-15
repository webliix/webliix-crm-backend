package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("CREATE_TASK")
public class CreateTaskAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateTaskAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String name = String.valueOf(parameters.getOrDefault("name", "Automated Task"));
        String description = String.valueOf(parameters.getOrDefault("description", "Created by automation rule."));
        logger.info("Automation CREATE_TASK executed: name={}, description={}", name, description);
    }
}
