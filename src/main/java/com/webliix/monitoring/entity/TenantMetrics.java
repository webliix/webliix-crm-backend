package com.webliix.monitoring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "active_users")
    private Long activeUsers = 0L;

    @Column(name = "storage_used_gb")
    private Double storageUsedGb = 0.0;

    @Column(name = "api_calls_this_month")
    private Long apiCallsThisMonth = 0L;

    @Column(name = "ai_usage_tokens")
    private Long aiUsageTokens = 0L;

    @Column(name = "email_sent_count")
    private Long emailSentCount = 0L;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
