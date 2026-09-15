package com.webliix.projects.dto;

import com.webliix.projects.enums.ProjectTaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectTaskResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private ProjectTaskStatus status;
    private Long assignedTo;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
}

