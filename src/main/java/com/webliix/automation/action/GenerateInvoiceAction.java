package com.webliix.automation.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("GENERATE_INVOICE")
public class GenerateInvoiceAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(GenerateInvoiceAction.class);

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String customer = String.valueOf(parameters.getOrDefault("customer", "unknown"));
        logger.info("Automation GENERATE_INVOICE executed for customer={}", customer);
    }
}
