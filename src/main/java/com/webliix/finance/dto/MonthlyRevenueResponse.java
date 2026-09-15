package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyRevenueResponse {
    private String month;
    private BigDecimal revenue;
}
