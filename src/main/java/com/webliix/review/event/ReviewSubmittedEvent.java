package com.webliix.review.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewSubmittedEvent extends ApplicationEvent {

    private final String authorName;
    private final String companyName;
    private final Integer rating;
    private final String platform;

    public ReviewSubmittedEvent(Object source, String authorName, String companyName, Integer rating, String platform) {
        super(source);
        this.authorName = authorName;
        this.companyName = companyName;
        this.rating = rating;
        this.platform = platform;
    }
}
