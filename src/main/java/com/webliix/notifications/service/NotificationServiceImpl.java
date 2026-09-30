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
    public NotificationResponse broadcastNotification(CreateNotificationRequest request) {
        String recipientTarget = (request.getRecipient() != null && !request.getRecipient().isBlank())
                ? request.getRecipient().trim() : "ALL";

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .recipient(recipientTarget)
                .recipientType(request.getRecipientType() != null ? request.getRecipientType() : "BROADCAST")
                .channel(request.getChannel() != null ? request.getChannel() : com.webliix.notifications.enums.NotificationChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .referenceType(request.getReferenceType() != null ? request.getReferenceType() : "SYSTEM")
                .referenceId(request.getReferenceId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toResponse(saved);
    }

    @Override
    public List<NotificationResponse> getNotificationsForUser(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return getNotifications("admin@webliix.in");
        }

        String userEmail = auth.getName().trim().toLowerCase();
        java.util.Set<String> targets = new java.util.HashSet<>();
        targets.add(userEmail);
        targets.add("ALL");
        targets.add("ALL_USERS");

        if (auth.getAuthorities() != null) {
            auth.getAuthorities().forEach(granted -> {
                String authName = granted.getAuthority();
                targets.add(authName);
                if (authName.startsWith("ROLE_")) {
                    targets.add(authName.substring(5));
                }
            });
        }

        List<Notification> notifications = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);

        if (notifications.isEmpty()) {
            return getNotifications(userEmail);
        }

        return notifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient.trim();
        java.util.Set<String> targets = new java.util.HashSet<>(java.util.List.of(rec, rec.toLowerCase(), "ALL", "ALL_USERS", "BROADCAST"));
        List<Notification> list = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);

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
            list = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);
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
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient.trim();
        java.util.Set<String> targets = new java.util.HashSet<>(java.util.List.of(rec, rec.toLowerCase(), "ALL", "ALL_USERS", "BROADCAST"));
        List<Notification> all = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);
        notificationRepository.deleteAll(all);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient.trim();
        java.util.Set<String> targets = new java.util.HashSet<>(java.util.List.of(rec, rec.toLowerCase(), "ALL", "ALL_USERS", "BROADCAST"));
        return notificationRepository.findByRecipientInAndStatusOrderByCreatedAtDesc(targets, NotificationStatus.PENDING)
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
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.in" : recipient.trim();
        java.util.Set<String> targets = new java.util.HashSet<>(java.util.List.of(rec, rec.toLowerCase(), "ALL", "ALL_USERS", "BROADCAST"));
        List<Notification> unread = notificationRepository.findByRecipientInAndStatusOrderByCreatedAtDesc(targets, NotificationStatus.PENDING);
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
        Long targetId = userId != null ? userId : 1L;
        NotificationPreference preference = preferenceRepository.findByUserId(targetId)
                .orElseGet(() -> createDefaultPreference(targetId));
        return NotificationMapper.toResponse(preference);
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(NotificationPreferenceRequest request) {
        Long userId = request.getUserId() != null ? request.getUserId() : 1L;
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(userId));

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

    private NotificationPreference createDefaultPreference(Long userId) {
        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(false)
                .whatsappEnabled(false)
                .pushEnabled(true)
                .ticketNotifications(true)
                .invoiceNotifications(true)
                .projectNotifications(true)
                .leadNotifications(true)
                .payrollNotifications(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return preferenceRepository.save(pref);
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
