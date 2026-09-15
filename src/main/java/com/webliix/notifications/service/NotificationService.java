package com.webliix.notifications.service;

import com.webliix.notifications.dto.*;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    List<NotificationResponse> getNotifications(String recipient);

    List<NotificationResponse> getUnreadNotifications(String recipient);

    NotificationResponse getNotification(Long id);

    NotificationResponse markAsRead(Long id);

    void markAllAsRead(String recipient);

    NotificationDashboardResponse getDashboard();

    NotificationPreferenceResponse getPreferences(Long userId);

    NotificationPreferenceResponse updatePreferences(NotificationPreferenceRequest request);

    NotificationPreferenceResponse createPreferences(NotificationPreferenceRequest request);

    void deleteNotification(Long id);

    void clearAllNotifications(String recipient);
}
