package com.webliix.automation.trigger;

import com.webliix.automation.service.AutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AutomationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AutomationEventListener.class);
    private final AutomationService automationService;

    public AutomationEventListener(AutomationService automationService) {
        this.automationService = automationService;
    }

    @EventListener
    public void handleAutomationTriggerEvent(AutomationTriggerEvent event) {
        logger.info("Received automation trigger event: {}", event.getTriggerType());
        automationService.executeTrigger(event.getTriggerType(), event.getPayload());
    }
}
