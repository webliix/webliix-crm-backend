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
            return getNotifications("admin@webliix.com");
        }

        String userEmail = auth.getName().trim().toLowerCase();
        boolean isStaff = auth.getAuthorities() != null && auth.getAuthorities().stream().anyMatch(a -> {
            String name = a.getAuthority().toUpperCase();
            return name.contains("ADMIN") || name.contains("EMPLOYEE") || name.contains("MANAGER") || name.contains("HR") || name.equals("*");
        });

        java.util.Set<String> targets = new java.util.HashSet<>();
        targets.add(userEmail);

        if (isStaff) {
            targets.add("ALL");
            targets.add("ALL_USERS");
            targets.add("ALL_STAFF");
            targets.add("BROADCAST");
            if (auth.getAuthorities() != null) {
                auth.getAuthorities().forEach(granted -> {
                    String authName = granted.getAuthority();
                    targets.add(authName);
                    if (authName.startsWith("ROLE_")) {
                        targets.add(authName.substring(5));
                    }
                });
            }
        } else {
            // Strictly client / customer scope: only personal and broadcast client announcements
            targets.add("ALL_CLIENTS");
            targets.add("BROADCAST_CLIENTS");
            targets.add("CLIENT");
            targets.add("ROLE_CLIENT");
        }

        List<Notification> notifications = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);

        // Security filter: Ensure clients NEVER receive internal lead notifications, payroll, reviews, or employee alerts
        if (!isStaff) {
            notifications = notifications.stream().filter(n -> {
                String ref = n.getReferenceType() != null ? n.getReferenceType().toUpperCase() : "";
                String recType = n.getRecipientType() != null ? n.getRecipientType().toUpperCase() : "";
                if ("LEAD".equals(ref) || "PAYROLL".equals(ref) || "REVIEW".equals(ref) || "NEWSLETTER".equals(ref)) {
                    return false;
                }
                if ("ADMIN".equals(recType) || "EMPLOYEE".equals(recType) || "STAFF".equals(recType)) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
        }

        return notifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.com" : recipient.trim().toLowerCase();
        boolean looksLikeAdmin = rec.contains("admin") || rec.contains("webliix.com");
        
        java.util.Set<String> targets = new java.util.HashSet<>();
        targets.add(rec);
        targets.add(recipient != null ? recipient.trim() : "");
        if (looksLikeAdmin) {
            targets.addAll(java.util.List.of("ALL", "ALL_USERS", "BROADCAST", "ROLE_ADMIN", "ADMIN"));
        } else {
            targets.addAll(java.util.List.of("ALL_CLIENTS", "BROADCAST_CLIENTS", "CLIENT", "ROLE_CLIENT"));
        }
        
        List<Notification> list = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);

        if (!looksLikeAdmin) {
            list = list.stream().filter(n -> {
                String ref = n.getReferenceType() != null ? n.getReferenceType().toUpperCase() : "";
                String recType = n.getRecipientType() != null ? n.getRecipientType().toUpperCase() : "";
                return !"LEAD".equals(ref) && !"PAYROLL".equals(ref) && !"REVIEW".equals(ref) && !"NEWSLETTER".equals(ref)
                        && !"ADMIN".equals(recType) && !"EMPLOYEE".equals(recType) && !"STAFF".equals(recType);
            }).collect(Collectors.toList());
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
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.com" : recipient.trim().toLowerCase();
        java.util.Set<String> targets = new java.util.HashSet<>(java.util.List.of(rec));
        List<Notification> all = notificationRepository.findByRecipientInOrderByCreatedAtDesc(targets);
        notificationRepository.deleteAll(all);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(String recipient) {
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.com" : recipient.trim().toLowerCase();
        boolean looksLikeAdmin = rec.contains("admin") || rec.contains("webliix.com");
        java.util.Set<String> targets = new java.util.HashSet<>();
        targets.add(rec);
        if (looksLikeAdmin) {
            targets.addAll(java.util.List.of("ALL", "ALL_USERS", "BROADCAST", "ROLE_ADMIN", "ADMIN"));
        } else {
            targets.addAll(java.util.List.of("ALL_CLIENTS", "BROADCAST_CLIENTS", "CLIENT", "ROLE_CLIENT"));
        }
        return notificationRepository.findByRecipientInAndStatusOrderByCreatedAtDesc(targets, NotificationStatus.PENDING)
                .stream()
                .filter(n -> {
                    if (looksLikeAdmin) return true;
                    String ref = n.getReferenceType() != null ? n.getReferenceType().toUpperCase() : "";
                    String recType = n.getRecipientType() != null ? n.getRecipientType().toUpperCase() : "";
                    return !"LEAD".equals(ref) && !"PAYROLL".equals(ref) && !"REVIEW".equals(ref) && !"NEWSLETTER".equals(ref)
                            && !"ADMIN".equals(recType) && !"EMPLOYEE".equals(recType) && !"STAFF".equals(recType);
                })
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
        String rec = (recipient == null || recipient.isBlank()) ? "admin@webliix.com" : recipient.trim();
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
