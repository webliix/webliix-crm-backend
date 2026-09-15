package com.webliix.hr.leave.service.impl;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.leave.dto.LeaveBalanceRequest;
import com.webliix.hr.leave.dto.LeaveBalanceResponse;
import com.webliix.hr.leave.entity.LeaveBalance;
import com.webliix.hr.leave.repository.LeaveBalanceRepository;
import com.webliix.hr.leave.service.LeaveBalanceService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public LeaveBalanceResponse createLeaveBalance(LeaveBalanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeAndLeaveType(employee, request.getLeaveType())
                .orElse(LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(request.getLeaveType())
                        .usedDays(java.math.BigDecimal.ZERO)
                        .build());

        leaveBalance.setAllocationDays(request.getAllocationDays());
        if (leaveBalance.getUsedDays() == null) {
            leaveBalance.setUsedDays(java.math.BigDecimal.ZERO);
        }
        leaveBalance.setRemainingDays(request.getAllocationDays().subtract(leaveBalance.getUsedDays()));
        leaveBalance.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(leaveBalanceRepository.save(leaveBalance));
    }

    @Override
    public Page<LeaveBalanceResponse> getAllLeaveBalances(Pageable pageable) {
        return leaveBalanceRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public LeaveBalanceResponse getLeaveBalance(Long id) {
        return leaveBalanceRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found with id: " + id));
    }

    private LeaveBalanceResponse mapToResponse(LeaveBalance leaveBalance) {
        return LeaveBalanceResponse.builder()
                .id(leaveBalance.getId())
                .employee(mapEmployee(leaveBalance.getEmployee()))
                .leaveType(leaveBalance.getLeaveType())
                .allocationDays(leaveBalance.getAllocationDays())
                .usedDays(leaveBalance.getUsedDays())
                .remainingDays(leaveBalance.getRemainingDays())
                .updatedAt(leaveBalance.getUpdatedAt())
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
