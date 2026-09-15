package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("CREATE_TICKET")
public class CreateTicketAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateTicketAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String summary = String.valueOf(parameters.getOrDefault("summary", "Automated ticket created"));
        String priority = String.valueOf(parameters.getOrDefault("priority", "Medium"));
        logger.info("Automation CREATE_TICKET executed: summary={}, priority={}", summary, priority);
    }
}
