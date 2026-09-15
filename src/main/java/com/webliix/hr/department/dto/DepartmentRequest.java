package com.webliix.hr.department.dto;

import lombok.Data;

@Data
public class DepartmentRequest {
    private String departmentCode;
    private String departmentName;
    private String description;
    private Boolean active;
}
