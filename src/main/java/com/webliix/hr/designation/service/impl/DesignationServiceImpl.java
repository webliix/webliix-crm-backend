package com.webliix.hr.designation.service.impl;

import com.webliix.hr.department.entity.Department;
import com.webliix.hr.department.repository.DepartmentRepository;
import com.webliix.hr.designation.dto.DesignationRequest;
import com.webliix.hr.designation.dto.DesignationResponse;
import com.webliix.hr.designation.entity.Designation;
import com.webliix.hr.designation.repository.DesignationRepository;
import com.webliix.hr.designation.service.DesignationService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public DesignationResponse createDesignation(DesignationRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Designation designation = Designation.builder()
                .designationCode(request.getDesignationCode())
                .designationName(request.getDesignationName())
                .department(department)
                .description(request.getDescription())
                .build();
        Designation saved = designationRepository.save(designation);
        return toResponse(saved);
    }

    @Override
    public Page<DesignationResponse> getAllDesignations(Pageable pageable) {
        return designationRepository.findAll(pageable).map(this::toResponse);
    }

    private DesignationResponse toResponse(Designation designation) {
        DesignationResponse response = new DesignationResponse();
        response.setId(designation.getId());
        response.setDesignationCode(designation.getDesignationCode());
        response.setDesignationName(designation.getDesignationName());
        if (designation.getDepartment() != null) {
            response.setDepartmentId(designation.getDepartment().getId());
            response.setDepartmentName(designation.getDepartment().getDepartmentName());
        }
        response.setDescription(designation.getDescription());
        return response;
    }
}
