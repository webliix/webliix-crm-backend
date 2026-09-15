package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
}
