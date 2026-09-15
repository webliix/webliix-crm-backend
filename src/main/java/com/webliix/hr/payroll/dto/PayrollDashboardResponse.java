package com.webliix.hr.payroll.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDashboardResponse {

    private long totalPayrolls;
    private BigDecimal totalGrossSalary;
    private BigDecimal totalNetSalary;
    private BigDecimal totalDeductions;
}
