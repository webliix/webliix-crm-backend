package com.webliix.notifications.controller;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.service.NotificationService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification created successfully")
                .data(response)
                .build());
    }

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        List<NotificationResponse> response = notificationService.getNotifications("admin@webliix.in");
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{recipient}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(@PathVariable String recipient) {
        List<NotificationResponse> response = notificationService.getNotifications(recipient);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Notification deleted")
                .build());
    }

    @DeleteMapping({"/clear/{recipient}", "/clear"})
    public ResponseEntity<ApiResponse<Void>> clearAllNotifications(@PathVariable(required = false) String recipient) {
        notificationService.clearAllNotifications(recipient != null ? recipient : "admin@webliix.in");
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications cleared")
                .build());
    }

    @GetMapping("/{recipient}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(@PathVariable String recipient) {
        List<NotificationResponse> response = notificationService.getUnreadNotifications(recipient);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Unread notifications fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read")
                .data(response)
                .build());
    }

    @PutMapping("/{recipient}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable String recipient) {
        notificationService.markAllAsRead(recipient);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read")
                .build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<NotificationDashboardResponse>> getDashboard() {
        NotificationDashboardResponse response = notificationService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<NotificationDashboardResponse>builder()
                .success(true)
                .message("Notification dashboard fetched")
                .data(response)
                .build());
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(@PathVariable Long userId) {
        NotificationPreferenceResponse response = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences fetched")
                .data(response)
                .build());
    }

    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> createPreferences(@Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationService.createPreferences(request);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences created")
                .data(response)
                .build());
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(@Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationService.updatePreferences(request);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences updated")
                .data(response)
                .build());
    }
}
