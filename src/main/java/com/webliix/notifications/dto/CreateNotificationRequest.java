package com.webliix.notifications.dto;

import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.enums.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotBlank
    private String recipient;

    private String recipientType;

    @NotNull
    private NotificationChannel channel;

    private NotificationStatus status;

    private String referenceType;

    private Long referenceId;
}
