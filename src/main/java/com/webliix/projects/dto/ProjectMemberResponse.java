package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMemberResponse {

    private Long id;
    private Long projectId;
    private Long userId;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String employeeCode;
    private String designationName;
    private String departmentName;
    private String roleInProject;
    private LocalDate assignedDate;
}

