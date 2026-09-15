package com.webliix.hr.leave.dto;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.leave.enums.LeaveStatus;
import com.webliix.hr.leave.enums.LeaveType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveResponse {

    private Long id;
    private EmployeeResponse employee;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
