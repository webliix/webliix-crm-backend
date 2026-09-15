package com.webliix.hr.designation.dto;

import lombok.Data;

@Data
public class DesignationResponse {
    private Long id;
    private String designationCode;
    private String designationName;
    private Long departmentId;
    private String departmentName;
    private String description;
}
