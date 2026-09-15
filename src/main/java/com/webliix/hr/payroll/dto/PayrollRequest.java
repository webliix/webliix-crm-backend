package com.webliix.hr.payroll.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRequest {

    private Long employeeId;
    private String payrollMonth;
}
