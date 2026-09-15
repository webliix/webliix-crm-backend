package com.webliix.hr.department.service;

import com.webliix.hr.department.dto.DepartmentRequest;
import com.webliix.hr.department.dto.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    Page<DepartmentResponse> getAllDepartments(Pageable pageable);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
}
