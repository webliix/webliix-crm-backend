package com.webliix.notifications.service;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.entity.Notification;
import com.webliix.notifications.entity.NotificationPreference;
import com.webliix.notifications.enums.NotificationStatus;
import com.webliix.notifications.mapper.NotificationMapper;
import com.webliix.notifications.repository.NotificationPreferenceRepository;
import com.webliix.notifications.repository.NotificationRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        String recType = (request.getRecipientType() != null && !request.getRecipientType().isBlank())
                ? request.getRecipientType() : "USER";

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .recipient(request.getRecipient())
                .recipientType(recType)
                .channel(request.getChannel())
                .status(request.getStatus() != null ? request.getStatus() : NotificationStatus.PENDING)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        if (request.getChannel().name().equals("EMAIL")) {
            emailService.sendNotificationEmail(request.getRecipient(), request.getTitle(), request.getMessage());
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(LocalDateTime.now());
            notificationRepository.save(saved);
        }

        return NotificationMapper.toResponse(saved);
    }

    @Override
    public List<NotificationResponse> getNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient;
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(rec);

        if (list.isEmpty()) {
            // Seed initial persistent notifications into DB for recipient
            Notification n1 = Notification.builder()
                    .title("New High-Intent Lead Captured")
                    .message("Rahul Sharma submitted a high-value website development inquiry via Webliix LaunchKit.")
                    .recipient(rec)
                    .recipientType("USER")
                    .channel(com.webliix.notifications.enums.NotificationChannel.IN_APP)
                    .status(NotificationStatus.PENDING)
                    .referenceType("LEAD")
                    .referenceId(104L)
                    .createdAt(LocalDateTime.now().minusMinutes(10))
                    .updatedAt(LocalDateTime.now())
                    .build();

            Notification n2 = Notification.builder()
                    .title("New Public Blog Comment Posted")
                    .message("Anonymous Reader posted a comment on article 'Building High-Speed Microservices with Spring Boot 3'.")
                    .recipient(rec)
                    .recipientType("USER")
                    .channel(com.webliix.notifications.enums.NotificationChannel.IN_APP)
                    .status(NotificationStatus.PENDING)
                    .referenceType("BLOG")
                    .referenceId(1L)
                    .createdAt(LocalDateTime.now().minusMinutes(45))
                    .updatedAt(LocalDateTime.now())
                    .build();

            Notification n3 = Notification.builder()
                    .title("Security Session Renewal")
                    .message("Your administrator session was successfully authorized for persistent 7 days access.")
                    .recipient(rec)
                    .recipientType("USER")
                    .channel(com.webliix.notifications.enums.NotificationChannel.IN_APP)
                    .status(NotificationStatus.READ)
                    .referenceType("SECURITY")
                    .referenceId(1L)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .updatedAt(LocalDateTime.now())
                    .build();

            notificationRepository.saveAll(List.of(n1, n2, n3));
            list = notificationRepository.findByRecipientOrderByCreatedAtDesc(rec);
        }

        return list.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public void clearAllNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient;
        List<Notification> all = notificationRepository.findByRecipientOrderByCreatedAtDesc(rec);
        notificationRepository.deleteAll(all);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(String recipient) {
        return notificationRepository.findByRecipientAndStatusOrderByCreatedAtDesc(recipient, NotificationStatus.PENDING)
                .stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse getNotification(Long id) {
        Notification notification = findNotificationById(id);
        return NotificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        Notification notification = findNotificationById(id);
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);
        return NotificationMapper.toResponse(updated);
    }

    @Override
    public void markAllAsRead(String recipient) {
        List<Notification> unread = notificationRepository.findByRecipientAndStatusOrderByCreatedAtDesc(recipient, NotificationStatus.PENDING);
        unread.forEach(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
    }

    @Override
    public NotificationDashboardResponse getDashboard() {
        NotificationDashboardResponse response = new NotificationDashboardResponse();
        response.setTotalNotifications(notificationRepository.count());
        response.setSent(notificationRepository.countByStatus(NotificationStatus.SENT));
        response.setFailed(notificationRepository.countByStatus(NotificationStatus.FAILED));
        response.setPending(notificationRepository.countByStatus(NotificationStatus.PENDING));
        response.setUnread(notificationRepository.countByStatus(NotificationStatus.PENDING));
        return response;
    }

    @Override
    public NotificationPreferenceResponse getPreferences(Long userId) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification preference not found for user: " + userId));
        return NotificationMapper.toResponse(preference);
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification preference not found for user: " + request.getUserId()));

        if (request.getEmailEnabled() != null) preference.setEmailEnabled(request.getEmailEnabled());
        if (request.getSmsEnabled() != null) preference.setSmsEnabled(request.getSmsEnabled());
        if (request.getWhatsappEnabled() != null) preference.setWhatsappEnabled(request.getWhatsappEnabled());
        if (request.getPushEnabled() != null) preference.setPushEnabled(request.getPushEnabled());
        if (request.getTicketNotifications() != null) preference.setTicketNotifications(request.getTicketNotifications());
        if (request.getInvoiceNotifications() != null) preference.setInvoiceNotifications(request.getInvoiceNotifications());
        if (request.getProjectNotifications() != null) preference.setProjectNotifications(request.getProjectNotifications());
        if (request.getLeadNotifications() != null) preference.setLeadNotifications(request.getLeadNotifications());
        if (request.getPayrollNotifications() != null) preference.setPayrollNotifications(request.getPayrollNotifications());

        preference.setUpdatedAt(LocalDateTime.now());
        NotificationPreference updated = preferenceRepository.save(preference);
        return NotificationMapper.toResponse(updated);
    }

    @Override
    public NotificationPreferenceResponse createPreferences(NotificationPreferenceRequest request) {
        NotificationPreference preference = NotificationMapper.toEntity(request);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        NotificationPreference saved = preferenceRepository.save(preference);
        return NotificationMapper.toResponse(saved);
    }

    private Notification findNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }
}
