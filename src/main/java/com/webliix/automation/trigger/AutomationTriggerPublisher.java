package com.webliix.automation.trigger;

import com.webliix.automation.enums.TriggerType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class AutomationTriggerPublisher {

    private final ApplicationEventPublisher publisher;

    public AutomationTriggerPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(TriggerType triggerType, Map<String, Object> payload) {
        publisher.publishEvent(new AutomationTriggerEvent(this, triggerType, payload == null ? Collections.emptyMap() : payload));
    }
}
