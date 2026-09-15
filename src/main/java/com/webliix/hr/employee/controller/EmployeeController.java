package com.webliix.hr.employee.controller;

import com.webliix.hr.employee.dto.EmployeeRequest;
import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.dto.EmployeeStatisticsResponse;
import com.webliix.hr.employee.service.EmployeeService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder().success(true).message("Employee created successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getEmployees(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponse> response = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<EmployeeResponse>>builder().success(true).message("Employees fetched").data(response).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder().success(true).message("Employee fetched").data(response).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder().success(true).message("Employee updated successfully").data(response).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Employee deleted successfully").build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<EmployeeStatisticsResponse>> getStatistics() {
        EmployeeStatisticsResponse response = employeeService.getEmployeeStatistics();
        return ResponseEntity.ok(ApiResponse.<EmployeeStatisticsResponse>builder().success(true).message("Employee statistics fetched").data(response).build());
    }
}
