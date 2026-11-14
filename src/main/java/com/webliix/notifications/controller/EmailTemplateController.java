package com.webliix.notifications.controller;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.service.EmailTemplateService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    // =========================================================================
    // GLOBAL EMAIL BRAND LAYOUT (HEADER & FOOTER)
    // =========================================================================

    @GetMapping("/global-layout")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<GlobalEmailLayoutDto>> getGlobalLayout() {
        GlobalEmailLayoutDto layout = emailTemplateService.getGlobalLayout();
        return ResponseEntity.ok(ApiResponse.<GlobalEmailLayoutDto>builder()
                .success(true)
                .message("Global email header and footer retrieved")
                .data(layout)
                .build());
    }

    @PutMapping("/global-layout")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<GlobalEmailLayoutDto>> updateGlobalLayout(@RequestBody GlobalEmailLayoutDto dto) {
        GlobalEmailLayoutDto updated = emailTemplateService.updateGlobalLayout(dto);
        return ResponseEntity.ok(ApiResponse.<GlobalEmailLayoutDto>builder()
                .success(true)
                .message("Global email header and footer updated successfully")
                .data(updated)
                .build());
    }

    // =========================================================================
    // MASTER EMAIL TEMPLATES CRUD
    // =========================================================================

    @GetMapping
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllTemplates() {
        List<EmailTemplateResponse> list = emailTemplateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.<List<EmailTemplateResponse>>builder()
                .success(true)
                .message("Email templates fetched successfully")
                .data(list)
                .build());
    }

    @GetMapping("/{id:[0-9]+}")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getTemplate(@PathVariable("id") Long id) {
        EmailTemplateResponse response = emailTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id:[0-9]+}")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> updateTemplate(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateEmailTemplateRequest request) {
        EmailTemplateResponse response = emailTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{id:[0-9]+}/toggle")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> toggleTemplate(@PathVariable("id") Long id) {
        EmailTemplateResponse response = emailTemplateService.toggleTemplate(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template active state toggled")
                .data(response)
                .build());
    }

    @PostMapping("/{id:[0-9]+}/preview")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<String>> previewTemplate(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PreviewEmailTemplateRequest request) {
        String renderedHtml = emailTemplateService.previewTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Template rendered preview generated")
                .data(renderedHtml)
                .build());
    }

    @PostMapping("/{id:[0-9]+}/test-send")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<Boolean>> testSendTemplate(
            @PathVariable("id") Long id,
            @Valid @RequestBody TestSendEmailTemplateRequest request) {
        boolean sent = emailTemplateService.testSendTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(sent)
                .message(sent ? "Test email dispatched successfully" : "Failed to send test email")
                .data(sent)
                .build());
    }

    @PostMapping("/{id:[0-9]+}/reset")
    @RequirePermission("EMAIL_TEMPLATES_MANAGE")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> resetTemplate(@PathVariable("id") Long id) {
        EmailTemplateResponse response = emailTemplateService.resetTemplateToDefault(id);
        return ResponseEntity.ok(ApiResponse.<EmailTemplateResponse>builder()
                .success(true)
                .message("Email template restored to default")
                .data(response)
                .build());
    }
}
