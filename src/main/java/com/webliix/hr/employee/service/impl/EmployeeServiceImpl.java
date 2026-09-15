package com.webliix.hr.employee.service.impl;

import com.webliix.hr.department.entity.Department;
import com.webliix.hr.department.repository.DepartmentRepository;
import com.webliix.hr.designation.entity.Designation;
import com.webliix.hr.designation.repository.DesignationRepository;
import com.webliix.hr.employee.dto.EmployeeRequest;
import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.dto.EmployeeStatisticsResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.employee.service.EmployeeService;
import com.webliix.hr.employee.util.EmployeeCodeGenerator;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        String prefix = EmployeeCodeGenerator.currentPrefix();
        Employee last = employeeRepository.findTopByEmployeeCodeStartingWithOrderByIdDesc(prefix);
        String employeeCode = last != null
                ? EmployeeCodeGenerator.next(last.getEmployeeCode())
                : prefix + "000001";

        Boolean active = request.getActive();
        if (active == null) {
            active = Boolean.TRUE;
        }

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(department)
                .designation(designation)
                .joiningDate(request.getJoiningDate())
                .salary(request.getSalary())
                .employmentType(request.getEmploymentType())
                .active(active)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .emergencyContact(request.getEmergencyContact())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Override
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Designation designation = designationRepository.findById(request.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setJoiningDate(request.getJoiningDate());
        employee.setSalary(request.getSalary());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setActive(request.getActive() != null ? request.getActive() : employee.getActive());
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setUpdatedAt(LocalDateTime.now());

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeStatisticsResponse getEmployeeStatistics() {
        List<Employee> employees = employeeRepository.findAll();
        long total = employees.size();
        long active = employees.stream().filter(emp -> Boolean.TRUE.equals(emp.getActive())).count();
        long inactive = total - active;
        long newThisMonth = employees.stream()
                .map(Employee::getJoiningDate)
                .filter(date -> date != null && date.getMonthValue() == LocalDate.now().getMonthValue() && date.getYear() == LocalDate.now().getYear())
                .count();

        EmployeeStatisticsResponse response = new EmployeeStatisticsResponse();
        response.setTotalEmployees(total);
        response.setActiveEmployees(active);
        response.setInactiveEmployees(inactive);
        response.setNewThisMonth(newThisMonth);
        return response;
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getDepartmentName());
        }
        if (employee.getDesignation() != null) {
            response.setDesignationId(employee.getDesignation().getId());
            response.setDesignationName(employee.getDesignation().getDesignationName());
        }
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
