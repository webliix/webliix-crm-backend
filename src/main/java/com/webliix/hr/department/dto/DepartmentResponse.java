package com.webliix.hr.department.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentResponse {
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
