package com.webliix.hr.leave.dto;

import com.webliix.hr.leave.enums.LeaveType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceRequest {

    private Long employeeId;
    private LeaveType leaveType;
    private BigDecimal allocationDays;
}
