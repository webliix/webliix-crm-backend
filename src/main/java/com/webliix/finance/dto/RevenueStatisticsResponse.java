package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueStatisticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalPending;
    private BigDecimal totalPaid;
    private Long totalInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;
}
