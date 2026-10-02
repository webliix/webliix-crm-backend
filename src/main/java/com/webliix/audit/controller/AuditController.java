package com.webliix.audit.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webliix.audit.dto.AuditDashboardResponse;
import com.webliix.audit.dto.AuditLogResponse;
import com.webliix.audit.dto.AuditSearchRequest;
import com.webliix.audit.dto.UserActivityResponse;
import com.webliix.audit.service.AuditService;
import com.webliix.shared.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping({"/audit", "/audit/logs", "/audit-logs"})
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> searchAuditGet(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        AuditSearchRequest searchRequest = AuditSearchRequest.builder()
                .module(module)
                .action(action)
                .userId(userId)
                .status(status)
                .entityType(entityType)
                .entityId(entityId)
                .page(page)
                .size(size)
                .build();
        Page<AuditLogResponse> result = auditService.search(searchRequest);
        ApiResponse<Page<AuditLogResponse>> response = ApiResponse.<Page<AuditLogResponse>>builder()
                .success(true)
                .message("Audit logs fetched")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/audit", "/audit/logs", "/audit-logs", "/audit/search"})
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> searchAuditPost(
            @RequestBody(required = false) AuditSearchRequest searchRequest
    ) {
        if (searchRequest == null) {
            searchRequest = new AuditSearchRequest();
        }
        Page<AuditLogResponse> result = auditService.search(searchRequest);
        ApiResponse<Page<AuditLogResponse>> response = ApiResponse.<Page<AuditLogResponse>>builder()
                .success(true)
                .message("Audit logs fetched")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit/dashboard")
    public ResponseEntity<ApiResponse<AuditDashboardResponse>> getDashboard() {
        ApiResponse<AuditDashboardResponse> response = ApiResponse.<AuditDashboardResponse>builder()
                .success(true)
                .message("Audit dashboard stats")
                .data(auditService.getDashboard())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/activity")
    public ResponseEntity<ApiResponse<List<UserActivityResponse>>> getUserActivity(@PathVariable("id") Long userId) {
        ApiResponse<List<UserActivityResponse>> response = ApiResponse.<List<UserActivityResponse>>builder()
                .success(true)
                .message("User activity fetched")
                .data(auditService.getUserActivity(userId))
                .build();
        return ResponseEntity.ok(response);
    }
}

