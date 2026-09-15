package com.webliix.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDashboardResponse {

    private long totalNotifications;
    private long sent;
    private long failed;
    private long pending;
    private long unread;
}
