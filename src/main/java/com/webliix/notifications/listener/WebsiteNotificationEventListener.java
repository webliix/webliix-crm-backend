package com.webliix.notifications.listener;

import com.webliix.newsletter.event.NewsletterSubscribedEvent;
import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.service.NotificationService;
import com.webliix.review.event.ReviewSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsiteNotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleNewsletterSubscribed(NewsletterSubscribedEvent event) {
        log.info("Processing NewsletterSubscribedEvent for subscriber {}", event.getEmail());
        try {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title("New Newsletter Subscriber")
                    .message("User (" + event.getEmail() + ") subscribed to Webliix updates on page: " + (event.getSourcePage() != null ? event.getSourcePage() : "/"))
                    .recipient("ALL")
                    .recipientType("BROADCAST")
                    .channel(NotificationChannel.IN_APP)
                    .referenceType("NEWSLETTER")
                    .build();

            notificationService.broadcastNotification(request);
        } catch (Exception ex) {
            log.error("Failed to broadcast newsletter notification: {}", ex.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleReviewSubmitted(ReviewSubmittedEvent event) {
        log.info("Processing ReviewSubmittedEvent by author {}", event.getAuthorName());
        try {
            String title = "New Client Review Received (" + event.getRating() + " Stars)";
            String msg = "Review submitted by " + event.getAuthorName()
                    + (event.getCompanyName() != null ? " (" + event.getCompanyName() + ")" : "")
                    + " via platform " + event.getPlatform();

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .message(msg)
                    .recipient("ALL")
                    .recipientType("BROADCAST")
                    .channel(NotificationChannel.IN_APP)
                    .referenceType("REVIEW")
                    .build();

            notificationService.broadcastNotification(request);
        } catch (Exception ex) {
            log.error("Failed to broadcast review notification: {}", ex.getMessage());
        }
    }
}
