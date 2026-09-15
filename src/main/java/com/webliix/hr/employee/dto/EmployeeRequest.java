package com.webliix.hr.employee.dto;

import com.webliix.hr.employee.enums.EmploymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Long departmentId;
    private Long designationId;
    private LocalDate joiningDate;
    private BigDecimal salary;
    private EmploymentType employmentType;
    private Boolean active;
    private String address;
    private String city;
    private String state;
    private String country;
    private String emergencyContact;
}
