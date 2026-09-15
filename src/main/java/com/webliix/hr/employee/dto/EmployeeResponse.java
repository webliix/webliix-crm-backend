package com.webliix.hr.employee.dto;

import com.webliix.hr.employee.enums.EmploymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private LocalDate joiningDate;
    private BigDecimal salary;
    private EmploymentType employmentType;
    private Boolean active;
    private String address;
    private String city;
    private String state;
    private String country;
    private String emergencyContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
