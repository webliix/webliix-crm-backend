package com.webliix.hr.department.controller;

import com.webliix.hr.department.dto.DepartmentRequest;
import com.webliix.hr.department.dto.DepartmentResponse;
import com.webliix.hr.department.service.DepartmentService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.<DepartmentResponse>builder().success(true).message("Department created successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> getDepartments(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentResponse> response = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<DepartmentResponse>>builder().success(true).message("Departments fetched").data(response).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.<DepartmentResponse>builder().success(true).message("Department updated successfully").data(response).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Department deleted successfully").build());
    }
}
