package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMemberResponse {

    private Long id;
    private Long projectId;
    private Long userId;
    private String roleInProject;
    private LocalDate assignedDate;
}

