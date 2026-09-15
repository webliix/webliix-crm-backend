package com.webliix.billing.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponse {
    private Long id;
    private Long tenantId;
    private Long planId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenew;
}
