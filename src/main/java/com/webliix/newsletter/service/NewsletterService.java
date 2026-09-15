package com.webliix.newsletter.service;

import com.webliix.newsletter.dto.NewsletterSubscribeRequest;
import com.webliix.newsletter.dto.NewsletterSubscriberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsletterService {

    NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request);

    void unsubscribe(String token);

    Page<NewsletterSubscriberResponse> getAllSubscribers(Pageable pageable);
}
