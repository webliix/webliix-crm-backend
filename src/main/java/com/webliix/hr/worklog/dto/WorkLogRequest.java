package com.webliix.hr.worklog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WorkLogRequest {
    private LocalDate logDate;
    private String workSummary;
    private BigDecimal hoursWorked;
    private BigDecimal workUnits;
    private BigDecimal workCost;
    private Long projectId;
    private Long taskId;
    private String tasksCompleted;
    private String blockers;
}
