package com.webliix.notifications.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProjectUpdateEvent extends ApplicationEvent {

    private final Long projectId;
    private final String projectName;
    private final String customerEmail;
    private final String title;
    private final String message;
    private final Integer progressPercentage;
    private final String status;

    public ProjectUpdateEvent(Object source, Long projectId, String projectName, String customerEmail,
                              String title, String message, Integer progressPercentage, String status) {
        super(source);
        this.projectId = projectId;
        this.projectName = projectName;
        this.customerEmail = customerEmail;
        this.title = title;
        this.message = message;
        this.progressPercentage = progressPercentage;
        this.status = status;
    }
}
