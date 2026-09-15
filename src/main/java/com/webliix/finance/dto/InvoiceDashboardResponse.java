package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceDashboardResponse {
    private Long totalInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;
    private Long pendingInvoices;
    private BigDecimal totalRevenue;
}
