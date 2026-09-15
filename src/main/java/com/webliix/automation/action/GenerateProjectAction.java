package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("GENERATE_PROJECT")
public class GenerateProjectAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(GenerateProjectAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String name = String.valueOf(parameters.getOrDefault("name", "Automated project"));
        logger.info("Automation GENERATE_PROJECT executed: name={}", name);
    }
}
