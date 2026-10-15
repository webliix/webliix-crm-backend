package com.webliix.notifications.controller;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.service.NotificationService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.evaluator.PermissionEvaluator;
import com.webliix.security.scoping.DataScopeService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PermissionEvaluator permissionEvaluator;
    private final DataScopeService dataScopeService;

    @PostMapping
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification created successfully")
                .data(response)
                .build());
    }

    @PostMapping("/broadcast")
    @RequirePermission("NOTIFICATIONS_BROADCAST")
    public ResponseEntity<ApiResponse<NotificationResponse>> broadcastNotification(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.broadcastNotification(request);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification broadcast sent successfully")
                .data(response)
                .build());
    }

    @GetMapping({"", "/"})
    @RequirePermission(anyOf = {"DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Authentication authentication) {
        List<NotificationResponse> response = notificationService.getNotificationsForUser(authentication);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{recipient}")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @PathVariable String recipient,
            Authentication authentication) {
        if (!permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            if (authentication == null || !authentication.getName().equalsIgnoreCase(recipient)) {
                throw new AccessDeniedException("Cannot view notifications of other users");
            }
        }
        List<NotificationResponse> response = notificationService.getNotifications(recipient);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications fetched")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Notification deleted")
                .build());
    }

    @DeleteMapping({"/clear/{recipient}", "/clear"})
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<Void>> clearAllNotifications(
            @PathVariable(required = false) String recipient,
            Authentication authentication
    ) {
        if (recipient != null && !permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            if (authentication == null || !authentication.getName().equalsIgnoreCase(recipient)) {
                throw new AccessDeniedException("Cannot clear notifications of other users");
            }
        }
        String targetRecipient = recipient != null ? recipient : (authentication != null ? authentication.getName() : "");
        notificationService.clearAllNotifications(targetRecipient);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications cleared")
                .build());
    }

    @GetMapping({"/unread-count", "/{recipient}/unread-count"})
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<Integer>> getUnreadCount(
            @PathVariable(required = false) String recipient,
            Authentication authentication
    ) {
        if (recipient != null && !permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            if (authentication == null || !authentication.getName().equalsIgnoreCase(recipient)) {
                throw new AccessDeniedException("Cannot inspect unread count of other users");
            }
        }
        String target = recipient != null ? recipient : (authentication != null ? authentication.getName() : "ALL");
        List<NotificationResponse> unread = notificationService.getUnreadNotifications(target);
        int count = unread != null ? unread.size() : 0;
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .message("Unread count fetched")
                .data(count)
                .build());
    }

    @GetMapping("/{recipient}/unread")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable String recipient,
            Authentication authentication) {
        if (!permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            if (authentication == null || !authentication.getName().equalsIgnoreCase(recipient)) {
                throw new AccessDeniedException("Cannot read unread notifications of other users");
            }
        }
        List<NotificationResponse> response = notificationService.getUnreadNotifications(recipient);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Unread notifications fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/detail")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/read")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read")
                .data(response)
                .build());
    }

    @PutMapping({"/read-all", "/{recipient}/read-all"})
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @PathVariable(required = false) String recipient,
            Authentication authentication
    ) {
        if (recipient != null && !"read-all".equalsIgnoreCase(recipient) && !permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            if (authentication == null || !authentication.getName().equalsIgnoreCase(recipient)) {
                throw new AccessDeniedException("Cannot mark notifications as read for other users");
            }
        }
        String targetRecipient = (recipient != null && !recipient.isBlank() && !"read-all".equalsIgnoreCase(recipient))
                ? recipient
                : (authentication != null ? authentication.getName() : "");
        notificationService.markAllAsRead(targetRecipient);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read")
                .build());
    }

    @GetMapping("/dashboard")
    @RequirePermission("NOTIFICATIONS_BROADCAST")
    public ResponseEntity<ApiResponse<NotificationDashboardResponse>> getDashboard() {
        NotificationDashboardResponse response = notificationService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<NotificationDashboardResponse>builder()
                .success(true)
                .message("Notification dashboard fetched")
                .data(response)
                .build());
    }

    @GetMapping("/preferences/{userId}")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @PathVariable Long userId,
            Authentication auth) {
        if (!permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            Long currentUserId = dataScopeService.getCurrentUserId();
            if (currentUserId == null || !currentUserId.equals(userId)) {
                throw new AccessDeniedException("IDOR violation: Cannot access preferences of other users");
            }
        }
        NotificationPreferenceResponse response = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences fetched")
                .data(response)
                .build());
    }

    @PostMapping("/preferences")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> createPreferences(@Valid @RequestBody NotificationPreferenceRequest request) {
        if (!permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            Long currentUserId = dataScopeService.getCurrentUserId();
            if (currentUserId == null || (request.getUserId() != null && !currentUserId.equals(request.getUserId()))) {
                throw new AccessDeniedException("IDOR violation: Cannot configure preferences for other users");
            }
        }
        NotificationPreferenceResponse response = notificationService.createPreferences(request);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences created")
                .data(response)
                .build());
    }

    @PutMapping("/preferences")
    @RequirePermission(anyOf = {"NOTIFICATIONS_BROADCAST", "DASHBOARD_VIEW", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(@Valid @RequestBody NotificationPreferenceRequest request) {
        if (!permissionEvaluator.has("NOTIFICATIONS_BROADCAST")) {
            Long currentUserId = dataScopeService.getCurrentUserId();
            if (currentUserId == null || (request.getUserId() != null && !currentUserId.equals(request.getUserId()))) {
                throw new AccessDeniedException("IDOR violation: Cannot modify preferences of other users");
            }
        }
        NotificationPreferenceResponse response = notificationService.updatePreferences(request);
        return ResponseEntity.ok(ApiResponse.<NotificationPreferenceResponse>builder()
                .success(true)
                .message("Notification preferences updated")
                .data(response)
                .build());
    }
}
