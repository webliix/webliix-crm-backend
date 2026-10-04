package com.webliix.hr.worklog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkLogResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate logDate;
    private String workSummary;
    private BigDecimal hoursWorked;
    private Long projectId;
    private String projectName;
    private Long taskId;
    private String taskTitle;
    private String tasksCompleted;
    private String blockers;
    private String status;
    private String reviewedBy;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
