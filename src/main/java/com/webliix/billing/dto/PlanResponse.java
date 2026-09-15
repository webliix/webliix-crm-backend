package com.webliix.billing.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerMonth;
    private BigDecimal pricePerYear;
    private Integer maxUsers;
    private Integer maxStorageGb;
    private Long maxApiCallsPerMonth;
    private String features;
}
