package com.webliix.automation.trigger;

import com.webliix.automation.enums.TriggerType;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.Map;

public class AutomationTriggerEvent extends ApplicationEvent {

    private final TriggerType triggerType;
    private final Map<String, Object> payload;

    public AutomationTriggerEvent(Object source, TriggerType triggerType, Map<String, Object> payload) {
        super(source);
        this.triggerType = triggerType;
        this.payload = payload == null ? Collections.emptyMap() : payload;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
