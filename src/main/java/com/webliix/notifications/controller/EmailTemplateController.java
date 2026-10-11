package com.webliix.notifications.controller;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.service.EmailTemplateService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    // =========================================================================
    // EMAIL TEMPLATES API
    // =========================================================================

    @GetMapping("/email-templates")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        List<EmailTemplateResponse> list = emailTemplateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.<List<EmailTemplateResponse>>builder()
                .success(true)
                .message("Email templates fetched successfully")
                .data(list)
                .build());
    }

    @GetMapping("/email-templates/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getTemplate(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template fetched")
                .data(response)
                .build());
    }

    @PutMapping("/email-templates/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmailTemplateRequest request) {
        EmailTemplateResponse response = emailTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/email-templates/{id}/toggle")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> toggleTemplate(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.toggleTemplate(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template active state toggled")
                .data(response)
                .build());
    }

    @PostMapping("/email-templates/{id}/preview")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> previewTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) PreviewEmailTemplateRequest request) {
        String renderedHtml = emailTemplateService.previewTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Template rendered preview generated")
                .data(renderedHtml)
                .build());
    }

    @PostMapping("/email-templates/{id}/test-send")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> testSendTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TestSendEmailTemplateRequest request) {
        boolean sent = emailTemplateService.testSendTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(sent)
                .message(sent ? "Test email dispatched successfully" : "Failed to send test email")
                .data(sent)
                .build());
    }

    @PostMapping("/email-templates/{id}/reset")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> resetTemplate(@PathVariable Long id) {
        EmailTemplateResponse response = emailTemplateService.resetTemplateToDefault(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template restored to default")
                .data(response)
                .build());
    }

    // =========================================================================
    // NOTIFICATION & EMAIL DISPATCH RULES
    // =========================================================================

    @GetMapping("/settings/notifications")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationRulesResponse>> getNotificationRules() {
        NotificationRulesResponse response = emailTemplateService.getNotificationRules();
        return ResponseEntity.ok(ApiResponse.<NotificationRulesResponse>builder()
                .success(true)
                .message("Notification dispatch configuration fetched")
                .data(response)
                .build());
    }

    @PutMapping("/settings/notifications")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<NotificationRulesResponse>> updateNotificationRules(
            @RequestBody UpdateNotificationRulesRequest request) {
        NotificationRulesResponse response = emailTemplateService.updateNotificationRules(request);
        return ResponseEntity.ok(ApiResponse.<NotificationRulesResponse>builder()
                .success(true)
                .message("Notification dispatch settings saved successfully")
                .data(response)
                .build());
    }
}
