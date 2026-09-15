package com.webliix.hr.leave.dto;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.leave.enums.LeaveType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {

    private Long id;
    private EmployeeResponse employee;
    private LeaveType leaveType;
    private BigDecimal allocationDays;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
    private LocalDateTime updatedAt;
}
