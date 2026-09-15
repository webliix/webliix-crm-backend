package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectMilestoneResponse {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean completed;
    private LocalDateTime completedAt;
}

