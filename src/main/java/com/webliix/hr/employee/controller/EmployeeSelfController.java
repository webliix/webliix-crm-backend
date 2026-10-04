package com.webliix.hr.employee.controller;

import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.projects.dto.ProjectResponse;
import com.webliix.projects.service.ProjectMapper;
import com.webliix.projects.repository.ProjectMemberRepository;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.security.entity.User;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.shared.response.ApiResponse;
import com.webliix.tickets.dto.TicketResponse;
import com.webliix.tickets.mapper.TicketMapper;
import com.webliix.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/employee/me")
@RequiredArgsConstructor
public class EmployeeSelfController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TicketRepository ticketRepository;
    private final com.webliix.finance.service.InvoiceService invoiceService;

    private Employee resolveEmployee(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        User user = userRepository.findByEmail(auth.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No employee profile linked to your account. Contact your administrator."));
    }

    /**
     * GET /api/v1/employee/me
     * Returns the authenticated employee's profile
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        EmployeeResponse response = toResponse(employee);
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(true).message("Employee profile fetched").data(response).build());
    }

    /**
     * GET /api/v1/employee/me/projects
     * Returns the projects the authenticated employee is explicitly assigned to
     */
    @GetMapping("/projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        if (employee.getUser() == null) {
            return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                    .success(true).message("Assigned projects fetched").data(Collections.emptyList()).build());
        }

        List<Long> projectIds = projectMemberRepository.findByUserId(employee.getUser().getId())
                .stream()
                .map(pm -> pm.getProjectId())
                .distinct()
                .collect(Collectors.toList());

        List<ProjectResponse> projects = projectRepository.findAllById(projectIds)
                .stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true).message("Assigned projects fetched").data(projects).build());
    }

    /**
     * GET /api/v1/employee/me/tickets
     * Returns the tickets assigned to the authenticated employee
     */
    @GetMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        List<TicketResponse> tickets = ticketRepository.findByAssignedToIdOrderByCreatedAtDesc(employee.getId())
                .stream()
                .map(TicketMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                .success(true).message("Assigned tickets fetched").data(tickets).build());
    }

    /**
     * POST /api/v1/employee/me/projects/{projectId}/bill
     * Allows an assigned employee to submit a bill/invoice for their project
     */
    @PostMapping("/projects/{projectId}/bill")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.webliix.finance.dto.InvoiceResponse>> billProject(
            @PathVariable Long projectId,
            @RequestBody com.webliix.finance.dto.CreateInvoiceRequest request,
            Authentication auth) {
        request.setProjectId(projectId);
        com.webliix.finance.dto.InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.<com.webliix.finance.dto.InvoiceResponse>builder()
                .success(true)
                .message("Project bill / invoice generated successfully")
                .data(response)
                .build());
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
        response.setUserId(employee.getUser() != null ? employee.getUser().getId() : null);
        response.setHasLoginAccount(employee.getUser() != null);
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }
}
