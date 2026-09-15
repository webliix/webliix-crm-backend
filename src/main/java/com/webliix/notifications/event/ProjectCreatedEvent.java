package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectCreatedEvent extends ApplicationEvent {

    private final Long projectId;
    private final String projectName;
    private final Long customerId;
    private final String customerEmail;

    public ProjectCreatedEvent(Object source, Long projectId, String projectName, Long customerId, String customerEmail) {
        super(source);
        this.projectId = projectId;
        this.projectName = projectName;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
    }
}
