package com.webliix.projects.dto;

import com.webliix.projects.enums.ProjectPriority;
import com.webliix.projects.enums.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProjectRequest {

    private String projectName;
    private String description;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private ProjectPriority priority;
    private Long customerId;
    private Boolean billable;
}

