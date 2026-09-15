package com.webliix.newsletter.repository;

import com.webliix.newsletter.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmail(String email);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken);

    long countByStatus(String status);
}
