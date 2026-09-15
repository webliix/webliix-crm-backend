package com.webliix.hr.leave.service.impl;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.leave.dto.LeaveRequestDto;
import com.webliix.hr.leave.dto.LeaveResponse;
import com.webliix.hr.leave.entity.LeaveBalance;
import com.webliix.hr.leave.entity.LeaveRequest;
import com.webliix.hr.leave.enums.LeaveStatus;
import com.webliix.hr.leave.repository.LeaveBalanceRepository;
import com.webliix.hr.leave.repository.LeaveRequestRepository;
import com.webliix.hr.leave.service.LeaveService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public LeaveResponse applyLeave(LeaveRequestDto request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public Page<LeaveResponse> getAllLeaves(Pageable pageable) {
        return leaveRequestRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public LeaveResponse getLeave(Long id) {
        return leaveRequestRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        if (!LeaveStatus.PENDING.equals(leaveRequest.getStatus())) {
            throw new IllegalStateException("Leave request is not pending and cannot be approved");
        }

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveType(leaveRequest.getEmployee(), leaveRequest.getLeaveType())
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee and leave type"));

        long daysRequested = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        java.math.BigDecimal requestedDays = java.math.BigDecimal.valueOf(daysRequested);
        java.math.BigDecimal newUsedDays = balance.getUsedDays().add(requestedDays);
        java.math.BigDecimal remainingDays = balance.getAllocationDays().subtract(newUsedDays);
        if (remainingDays.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient leave balance to approve this request");
        }

        balance.setUsedDays(newUsedDays);
        balance.setRemainingDays(remainingDays);
        balance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepository.save(balance);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy("System");
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveResponse rejectLeave(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy("System");
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employee(mapEmployee(leaveRequest.getEmployee()))
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .approvedBy(leaveRequest.getApprovedBy())
                .approvedAt(leaveRequest.getApprovedAt())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    private EmployeeResponse mapEmployee(Employee employee) {
        if (Objects.isNull(employee)) {
            return null;
        }
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        response.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : null);
        response.setDesignationId(employee.getDesignation() != null ? employee.getDesignation().getId() : null);
        response.setDesignationName(employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null);
        response.setJoiningDate(employee.getJoiningDate());
        response.setSalary(employee.getSalary());
        response.setEmploymentType(employee.getEmploymentType());
        response.setActive(employee.getActive());
        response.setAddress(employee.getAddress());
        response.setCity(employee.getCity());
        response.setState(employee.getState());
        response.setCountry(employee.getCountry());
        response.setEmergencyContact(employee.getEmergencyContact());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }
}
