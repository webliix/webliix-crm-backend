package com.webliix.portal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/portal/dashboard")
@RequiredArgsConstructor
public class PortalDashboardController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> dashboard() {
        // Minimal implementation: return zeros. Integrate with services to compute real counts.
        Map<String, Object> payload = Map.of(
                "activeProjects", 0,
                "pendingInvoices", 0,
                "openTickets", 0,
                "files", 0
        );
        return ResponseEntity.ok(payload);
    }
}
