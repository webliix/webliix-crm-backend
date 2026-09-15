package com.webliix.hr.designation.dto;

import lombok.Data;

@Data
public class DesignationRequest {
    private String designationCode;
    private String designationName;
    private Long departmentId;
    private String description;
}
