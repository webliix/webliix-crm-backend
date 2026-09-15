package com.webliix.audit.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webliix.audit.dto.AuditDashboardResponse;
import com.webliix.audit.dto.AuditLogResponse;
import com.webliix.audit.dto.AuditSearchRequest;
import com.webliix.audit.dto.UserActivityResponse;
import com.webliix.audit.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogResponse>> searchAudit(@ModelAttribute AuditSearchRequest searchRequest) {
        return ResponseEntity.ok(auditService.search(searchRequest));
    }

    @GetMapping("/audit/dashboard")
    public ResponseEntity<AuditDashboardResponse> getDashboard() {
        return ResponseEntity.ok(auditService.getDashboard());
    }

    @GetMapping("/users/{id}/activity")
    public ResponseEntity<List<UserActivityResponse>> getUserActivity(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(auditService.getUserActivity(userId));
    }
}
