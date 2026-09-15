package com.webliix.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Long id;
    private Long userId;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean whatsappEnabled;
    private Boolean pushEnabled;
    private Boolean ticketNotifications;
    private Boolean invoiceNotifications;
    private Boolean projectNotifications;
    private Boolean leadNotifications;
    private Boolean payrollNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
