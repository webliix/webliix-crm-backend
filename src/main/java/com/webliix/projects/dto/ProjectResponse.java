package com.webliix.projects.dto;

import com.webliix.projects.enums.ProjectPriority;
import com.webliix.projects.enums.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {

    private Long id;
    private String projectCode;
    private String projectName;
    private String description;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private ProjectPriority priority;
    private Long customerId;
    private Integer progressPercentage;
    private Boolean billable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

