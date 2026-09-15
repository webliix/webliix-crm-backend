package com.webliix.notifications.repository;

import com.webliix.notifications.entity.Notification;
import com.webliix.notifications.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientAndStatusOrderByCreatedAtDesc(String recipient, NotificationStatus status);

    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);

    long countByRecipientAndStatus(String recipient, NotificationStatus status);

    long countByStatus(NotificationStatus status);

    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime dateTime);
}
