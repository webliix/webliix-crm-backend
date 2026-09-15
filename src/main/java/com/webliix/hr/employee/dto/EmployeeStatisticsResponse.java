package com.webliix.hr.employee.dto;

import lombok.Data;

@Data
public class EmployeeStatisticsResponse {
    private Long totalEmployees;
    private Long activeEmployees;
    private Long inactiveEmployees;
    private Long newThisMonth;
}
