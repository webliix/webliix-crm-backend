package com.webliix.billing.dto;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long tenantId;
    private Long planId;
}
