package com.webliix.hr.department.service.impl;

import com.webliix.hr.department.dto.DepartmentRequest;
import com.webliix.hr.department.dto.DepartmentResponse;
import com.webliix.hr.department.entity.Department;
import com.webliix.hr.department.repository.DepartmentRepository;
import com.webliix.hr.department.service.DepartmentService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Boolean active = request.getActive();
        if (active == null) {
            active = Boolean.TRUE;
        }
        Department department = Department.builder()
                .departmentCode(request.getDepartmentCode())
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .active(active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Override
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }
        department.setUpdatedAt(LocalDateTime.now());
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Override
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponse toResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setDepartmentCode(department.getDepartmentCode());
        response.setDepartmentName(department.getDepartmentName());
        response.setDescription(department.getDescription());
        response.setActive(department.getActive());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());
        return response;
    }
}
