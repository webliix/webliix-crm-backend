package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectMemberRequest {

    private Long userId;
    private String roleInProject;
    private LocalDate assignedDate;
}

