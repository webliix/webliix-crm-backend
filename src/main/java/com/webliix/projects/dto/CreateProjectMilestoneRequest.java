package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectMilestoneRequest {

    private String title;
    private String description;
    private LocalDate dueDate;
    private Boolean completed;
}

