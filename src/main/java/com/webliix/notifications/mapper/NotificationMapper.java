package com.webliix.notifications.mapper;

import com.webliix.notifications.dto.NotificationPreferenceRequest;
import com.webliix.notifications.dto.NotificationPreferenceResponse;
import com.webliix.notifications.dto.NotificationResponse;
import com.webliix.notifications.entity.Notification;
import com.webliix.notifications.entity.NotificationPreference;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .recipient(notification.getRecipient())
                .recipientType(notification.getRecipientType())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public static NotificationPreferenceResponse toResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .emailEnabled(preference.getEmailEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .whatsappEnabled(preference.getWhatsappEnabled())
                .pushEnabled(preference.getPushEnabled())
                .ticketNotifications(preference.getTicketNotifications())
                .invoiceNotifications(preference.getInvoiceNotifications())
                .projectNotifications(preference.getProjectNotifications())
                .leadNotifications(preference.getLeadNotifications())
                .payrollNotifications(preference.getPayrollNotifications())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    public static NotificationPreference toEntity(NotificationPreferenceRequest request) {
        return NotificationPreference.builder()
                .userId(request.getUserId())
                .emailEnabled(request.getEmailEnabled() != null ? request.getEmailEnabled() : true)
                .smsEnabled(request.getSmsEnabled() != null ? request.getSmsEnabled() : false)
                .whatsappEnabled(request.getWhatsappEnabled() != null ? request.getWhatsappEnabled() : false)
                .pushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : true)
                .ticketNotifications(request.getTicketNotifications() != null ? request.getTicketNotifications() : true)
                .invoiceNotifications(request.getInvoiceNotifications() != null ? request.getInvoiceNotifications() : true)
                .projectNotifications(request.getProjectNotifications() != null ? request.getProjectNotifications() : true)
                .leadNotifications(request.getLeadNotifications() != null ? request.getLeadNotifications() : true)
                .payrollNotifications(request.getPayrollNotifications() != null ? request.getPayrollNotifications() : true)
                .build();
    }
}
