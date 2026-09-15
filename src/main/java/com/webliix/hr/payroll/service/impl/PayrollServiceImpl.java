package com.webliix.hr.payroll.service.impl;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.payroll.dto.PayrollDashboardResponse;
import com.webliix.hr.payroll.dto.PayrollRequest;
import com.webliix.hr.payroll.dto.PayrollResponse;
import com.webliix.hr.payroll.entity.Payroll;
import com.webliix.hr.payroll.enums.PayrollStatus;
import com.webliix.hr.payroll.repository.PayrollRepository;
import com.webliix.hr.payroll.service.PayrollService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public PayrollResponse generatePayroll(PayrollRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        YearMonth payrollMonth = YearMonth.parse(request.getPayrollMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal grossSalary = employee.getSalary() != null ? employee.getSalary() : BigDecimal.ZERO;
        BigDecimal deductions = calculateDeductions(employee);
        BigDecimal netSalary = grossSalary.subtract(deductions);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .payrollMonth(payrollMonth.toString())
                .grossSalary(grossSalary)
                .deductions(deductions)
                .netSalary(netSalary)
                .status(grossSalary.compareTo(BigDecimal.ZERO) > 0 ? PayrollStatus.PAID : PayrollStatus.FAILED)
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(payrollRepository.save(payroll));
    }

    @Override
    public Page<PayrollResponse> getAllPayrolls(Pageable pageable) {
        return payrollRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public PayrollResponse getPayroll(Long id) {
        return payrollRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
    }

    @Override
    public Page<PayrollResponse> getPayrollsForEmployee(Long employeeId, Pageable pageable) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        return payrollRepository.findByEmployee(employee, pageable).map(this::mapToResponse);
    }

    @Override
    public PayrollDashboardResponse getPayrollDashboard() {
        BigDecimal totalGrossSalary = BigDecimal.ZERO;
        BigDecimal totalNetSalary = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        long totalPayrolls = payrollRepository.count();
        for (Payroll payroll : payrollRepository.findAll()) {
            totalGrossSalary = totalGrossSalary.add(payroll.getGrossSalary() != null ? payroll.getGrossSalary() : BigDecimal.ZERO);
            totalNetSalary = totalNetSalary.add(payroll.getNetSalary() != null ? payroll.getNetSalary() : BigDecimal.ZERO);
            totalDeductions = totalDeductions.add(payroll.getDeductions() != null ? payroll.getDeductions() : BigDecimal.ZERO);
        }

        return PayrollDashboardResponse.builder()
                .totalPayrolls(totalPayrolls)
                .totalGrossSalary(totalGrossSalary)
                .totalNetSalary(totalNetSalary)
                .totalDeductions(totalDeductions)
                .build();
    }

    private BigDecimal calculateDeductions(Employee employee) {
        if (Objects.isNull(employee.getSalary()) || employee.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // In the future, integrate leave deductions and benefits
        return BigDecimal.ZERO;
    }

    private PayrollResponse mapToResponse(Payroll payroll) {
        return PayrollResponse.builder()
                .id(payroll.getId())
                .employee(mapEmployee(payroll.getEmployee()))
                .payrollMonth(payroll.getPayrollMonth())
                .grossSalary(payroll.getGrossSalary())
                .deductions(payroll.getDeductions())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus())
                .processedAt(payroll.getProcessedAt())
                .createdAt(payroll.getCreatedAt())
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
