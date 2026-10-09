package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ExpenseStatisticsResponse {
    private BigDecimal totalExpenses;
    private BigDecimal monthToDateExpenses;
    private BigDecimal approvedExpenses;
    private BigDecimal pendingExpenses;
    private long totalCount;
    private Map<String, BigDecimal> categoryBreakdown;
}
