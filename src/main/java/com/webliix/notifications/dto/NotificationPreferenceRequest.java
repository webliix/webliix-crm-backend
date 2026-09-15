package com.webliix.notifications.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull
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
}
