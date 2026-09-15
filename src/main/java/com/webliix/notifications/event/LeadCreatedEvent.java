package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadCreatedEvent extends ApplicationEvent {

    private final Long leadId;
    private final String leadName;
    private final String email;

    public LeadCreatedEvent(Object source, Long leadId, String leadName, String email) {
        super(source);
        this.leadId = leadId;
        this.leadName = leadName;
        this.email = email;
    }
}
