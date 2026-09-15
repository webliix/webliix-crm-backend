package com.webliix.hr.employee.service;

import com.webliix.hr.employee.dto.EmployeeRequest;
import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.dto.EmployeeStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
    EmployeeResponse getEmployee(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);
    EmployeeStatisticsResponse getEmployeeStatistics();
}
