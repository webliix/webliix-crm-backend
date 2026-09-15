package com.webliix.notifications.dto;

import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String recipient;
    private String recipientType;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
