package com.webliix.projects.dto;

import com.webliix.projects.enums.ProjectTaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectTaskRequest {

    private String title;
    private String description;
    private ProjectTaskStatus status;
    private Long assignedTo;
    private LocalDate startDate;
    private LocalDate dueDate;
}

