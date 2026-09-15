package com.webliix.hr.payroll.dto;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.payroll.enums.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponse {

    private Long id;
    private EmployeeResponse employee;
    private String payrollMonth;
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
