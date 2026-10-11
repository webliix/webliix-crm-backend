package com.webliix.projects.dto;

import com.webliix.projects.enums.ProjectPriority;
import com.webliix.projects.enums.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProjectRequest {

    private String projectName;
    private String description;
    private BigDecimal budget;
    private String currency;
    private String billingType;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private ProjectPriority priority;
    private Long customerId;
    private Boolean billable;
    private String documentationUrl;
    private String architectureNotes;
    private Boolean autoGeneratePhases;

    private List<CreateProjectMilestoneRequest> initialMilestones;
    private List<CreateProjectTaskRequest> initialTasks;
    private List<Long> initialMemberEmployeeIds;
}
