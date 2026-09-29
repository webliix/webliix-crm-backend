package com.webliix.newsletter.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewsletterSubscribedEvent extends ApplicationEvent {

    private final String email;
    private final String sourcePage;

    public NewsletterSubscribedEvent(Object source, String email, String sourcePage) {
        super(source);
        this.email = email;
        this.sourcePage = sourcePage;
    }
}
