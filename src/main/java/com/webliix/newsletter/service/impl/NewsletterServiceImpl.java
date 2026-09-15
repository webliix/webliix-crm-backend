package com.webliix.newsletter.service.impl;

import com.webliix.newsletter.dto.NewsletterSubscribeRequest;
import com.webliix.newsletter.dto.NewsletterSubscriberResponse;
import com.webliix.newsletter.entity.NewsletterSubscriber;
import com.webliix.newsletter.repository.NewsletterSubscriberRepository;
import com.webliix.newsletter.service.NewsletterService;
import com.webliix.notifications.service.EmailService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public NewsletterSubscriberResponse subscribe(NewsletterSubscribeRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<NewsletterSubscriber> existing = subscriberRepository.findByEmail(email);

        if (existing.isPresent()) {
            NewsletterSubscriber sub = existing.get();
            if ("ACTIVE".equalsIgnoreCase(sub.getStatus())) {
                return toResponse(sub, "You are already subscribed to Webliix updates.");
            } else {
                sub.setStatus("ACTIVE");
                sub.setSubscribedAt(LocalDateTime.now());
                sub.setUnsubscribedAt(null);
                NewsletterSubscriber saved = subscriberRepository.save(sub);
                sendWelcomeEmail(saved);
                return toResponse(saved, "Subscription reactivated successfully!");
            }
        }

        String unsubscribeToken = UUID.randomUUID().toString();
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(email)
                .name(request.getName())
                .status("ACTIVE")
                .unsubscribeToken(unsubscribeToken)
                .subscribedAt(LocalDateTime.now())
                .build();

        NewsletterSubscriber saved = subscriberRepository.save(subscriber);
        sendWelcomeEmail(saved);

        return toResponse(saved, "Thank you for subscribing to Webliix updates!");
    }

    @Override
    @Transactional
    public void unsubscribe(String token) {
        NewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired unsubscribe token"));

        subscriber.setStatus("UNSUBSCRIBED");
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    @Override
    public Page<NewsletterSubscriberResponse> getAllSubscribers(Pageable pageable) {
        return subscriberRepository.findAll(pageable).map(s -> toResponse(s, null));
    }

    private void sendWelcomeEmail(NewsletterSubscriber sub) {
        try {
            String unsubscribeUrl = "https://webliix.com/newsletter/unsubscribe?token=" + sub.getUnsubscribeToken();
            String name = (sub.getName() != null && !sub.getName().isBlank()) ? sub.getName() : "there";
            String subject = "Welcome to Webliix Updates & Insights!";
            String body = "Hello " + name + ",\n\n"
                    + "Thank you for subscribing to Webliix Insights! You are now subscribed to receive our latest engineering articles, product updates, tech case studies, and enterprise software releases.\n\n"
                    + "We respect your inbox and will only send relevant, high-impact content.\n\n"
                    + "If you ever wish to stop receiving these emails, you can unsubscribe at any time using the link below:\n"
                    + unsubscribeUrl + "\n\n"
                    + "Warm regards,\n"
                    + "The Webliix Team\n"
                    + "https://webliix.in";

            emailService.sendEmail(sub.getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Failed to dispatch newsletter welcome email to {}: {}", sub.getEmail(), ex.getMessage());
        }
    }

    private NewsletterSubscriberResponse toResponse(NewsletterSubscriber sub, String message) {
        return NewsletterSubscriberResponse.builder()
                .id(sub.getId())
                .email(sub.getEmail())
                .name(sub.getName())
                .status(sub.getStatus())
                .message(message)
                .subscribedAt(sub.getSubscribedAt())
                .build();
    }
}
