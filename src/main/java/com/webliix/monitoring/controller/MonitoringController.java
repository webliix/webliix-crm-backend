package com.webliix.monitoring.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webliix.monitoring.repository.TenantMetricsRepository;
import com.webliix.shared.response.ApiResponse;
import com.webliix.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final TenantRepository tenantRepository;
    private final TenantMetricsRepository metricsRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonitoringDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Owner metrics
        dashboard.put("tenantCount", tenantRepository.count());
        dashboard.put("recordedAt", LocalDateTime.now());

        // Placeholder for metrics aggregation
        long totalActiveUsers = 0L;
        double totalStorageGb = 0.0;
        long totalApiCalls = 0L;
        long totalAiTokens = 0L;
        long totalEmailsSent = 0L;

        dashboard.put("totalActiveUsers", totalActiveUsers);
        dashboard.put("totalStorageGb", totalStorageGb);
        dashboard.put("totalApiCalls", totalApiCalls);
        dashboard.put("totalAiTokens", totalAiTokens);
        dashboard.put("totalEmailsSent", totalEmailsSent);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Monitoring dashboard")
                        .data(dashboard)
                        .build()
        );
    }
}
