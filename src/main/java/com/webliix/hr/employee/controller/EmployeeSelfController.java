package com.webliix.hr.employee.controller;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.service.ExpenseService;
import com.webliix.hr.employee.dto.EmployeeResponse;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.payroll.dto.PayrollResponse;
import com.webliix.hr.payroll.service.PayrollService;
import com.webliix.projects.dto.ProjectResponse;
import com.webliix.projects.repository.ProjectMemberRepository;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.projects.service.ProjectMapper;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.entity.User;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.shared.response.ApiResponse;
import com.webliix.tickets.dto.TicketResponse;
import com.webliix.tickets.mapper.TicketMapper;
import com.webliix.tickets.repository.TicketRepository;
import com.webliix.hr.attendance.entity.Attendance;
import com.webliix.hr.attendance.enums.AttendanceStatus;
import com.webliix.hr.attendance.repository.AttendanceRepository;
import com.webliix.hr.leave.dto.LeaveRequestDto;
import com.webliix.hr.leave.entity.LeaveBalance;
import com.webliix.hr.leave.entity.LeaveRequest;
import com.webliix.hr.leave.enums.LeaveStatus;
import com.webliix.hr.leave.enums.LeaveType;
import com.webliix.hr.leave.repository.LeaveBalanceRepository;
import com.webliix.hr.leave.repository.LeaveRequestRepository;
import com.webliix.hr.worklog.repository.WorkLogRepository;
import com.webliix.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/employee/me")
@RequiredArgsConstructor
@RequirePermission(anyOf = {"EMPLOYEE_PORTAL_ACCESS", "DASHBOARD_VIEW"})
public class EmployeeSelfController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TicketRepository ticketRepository;
    private final ExpenseService expenseService;
    private final PayrollService payrollService;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final WorkLogRepository workLogRepository;
    private final AuthService authService;

    private Employee resolveEmployee(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        String email = auth.getName().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return employeeRepository.findByUserId(user.getId())
                .or(() -> employeeRepository.findByEmail(email).map(emp -> {
                    if (emp.getUser() == null) {
                        emp.setUser(user);
                        return employeeRepository.save(emp);
                    }
                    return emp;
                }))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No employee profile linked to your account. Contact your administrator."));
    }

    /**
     * GET /api/v1/employee/me
     * Returns the authenticated employee's profile
     */
    @GetMapping
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
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        java.util.Set<Long> candidateIds = new java.util.HashSet<>();
        if (employee.getUser() != null) {
            candidateIds.add(employee.getUser().getId());
        }
        if (employee.getId() != null) {
            candidateIds.add(employee.getId());
        }

        if (candidateIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                    .success(true).message("Assigned projects fetched").data(Collections.emptyList()).build());
        }

        List<Long> projectIds = projectMemberRepository.findByUserIdIn(candidateIds)
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
     * POST /api/v1/employee/me/expenses
     * Submits an expense for the current authenticated user (forced OWN + PENDING)
     */
    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseResponse>> submitOwnExpense(@RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true).message("Expense submitted successfully").data(response).build());
    }

    /**
     * GET /api/v1/employee/me/expenses
     * Returns expenses created by the current authenticated user
     */
    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getMyExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExpenseResponse> response = expenseService.getMyExpenses(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ExpenseResponse>>builder()
                .success(true).message("Own expenses fetched").data(response).build());
    }

    /**
     * GET /api/v1/employee/me/payslips
     * Returns payslips for the current employee
     */
    @GetMapping("/payslips")
    @RequirePermission("PAYSLIP_VIEW_OWN")
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getMyPayslips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Employee employee = resolveEmployee(auth);
        Pageable pageable = PageRequest.of(page, size);
        Page<PayrollResponse> response = payrollService.getPayrollsForEmployee(employee.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PayrollResponse>>builder()
                .success(true).message("Payslips fetched").data(response).build());
    }

    /**
     * GET /api/v1/employee/me/attendance
     * Returns attendance history and today's status for the current employee
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyAttendance(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        LocalDate today = LocalDate.now();
        Optional<Attendance> todayRecord = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today);
        List<Attendance> recent = attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employee.getId())
                .stream().limit(15).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("today", todayRecord.orElse(null));
        String status = todayRecord.map(a -> {
            if (a.getCheckOutTime() != null) return "CLOCKED_OUT";
            if (a.getCheckInTime() != null) return "CLOCKED_IN";
            return a.getStatus() != null ? a.getStatus().name() : "PRESENT";
        }).orElse("NOT_CLOCKED_IN");
        data.put("status", status);
        data.put("recent", recent);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).message("Attendance records fetched").data(data).build());
    }

    /**
     * POST /api/v1/employee/me/attendance/clock-in
     * Records check-in for today
     */
    @PostMapping("/attendance/clock-in")
    public ResponseEntity<ApiResponse<Attendance>> clockIn(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today);
        Attendance att;
        if (existing.isPresent()) {
            att = existing.get();
            if (att.getCheckInTime() == null) {
                att.setCheckInTime(LocalDateTime.now());
            }
        } else {
            att = new Attendance();
            att.setEmployee(employee);
            att.setAttendanceDate(today);
            att.setCheckInTime(LocalDateTime.now());
            att.setStatus(AttendanceStatus.PRESENT);
        }
        Attendance saved = attendanceRepository.save(att);
        return ResponseEntity.ok(ApiResponse.<Attendance>builder()
                .success(true).message("Clocked in successfully").data(saved).build());
    }

    /**
     * POST /api/v1/employee/me/attendance/clock-out
     * Records check-out for today and computes hours worked
     */
    @PostMapping("/attendance/clock-out")
    public ResponseEntity<ApiResponse<Attendance>> clockOut(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        LocalDate today = LocalDate.now();
        Attendance att = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today)
                .orElseThrow(() -> new IllegalStateException("You must clock in before clocking out."));
        att.setCheckOutTime(LocalDateTime.now());
        if (att.getCheckInTime() != null) {
            long minutes = java.time.Duration.between(att.getCheckInTime(), att.getCheckOutTime()).toMinutes();
            att.setWorkingHours(minutes / 60.0);
        }
        Attendance saved = attendanceRepository.save(att);
        return ResponseEntity.ok(ApiResponse.<Attendance>builder()
                .success(true).message("Clocked out successfully").data(saved).build());
    }

    /**
     * GET /api/v1/employee/me/leaves
     * Returns submitted leave requests
     */
    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getMyLeaves(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        List<LeaveRequest> list = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        return ResponseEntity.ok(ApiResponse.<List<LeaveRequest>>builder()
                .success(true).message("Leave requests fetched").data(list).build());
    }

    /**
     * POST /api/v1/employee/me/leaves
     * Submits a new leave request
     */
    @PostMapping("/leaves")
    public ResponseEntity<ApiResponse<LeaveRequest>> applyLeave(@RequestBody LeaveRequestDto request, Authentication auth) {
        Employee employee = resolveEmployee(auth);
        LeaveRequest lr = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType() != null ? request.getLeaveType() : LeaveType.CASUAL)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        LeaveRequest saved = leaveRequestRepository.save(lr);
        return ResponseEntity.ok(ApiResponse.<LeaveRequest>builder()
                .success(true).message("Leave application submitted").data(saved).build());
    }

    /**
     * GET /api/v1/employee/me/leave-balances
     * Returns employee leave quotas and remaining days
     */
    @GetMapping("/leave-balances")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> getMyLeaveBalances(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployee(employee);
        if (balances.isEmpty()) {
            balances = List.of(
                    LeaveBalance.builder().employee(employee).leaveType(LeaveType.ANNUAL).allocationDays(BigDecimal.valueOf(18)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(18)).updatedAt(LocalDateTime.now()).build(),
                    LeaveBalance.builder().employee(employee).leaveType(LeaveType.SICK).allocationDays(BigDecimal.valueOf(12)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(12)).updatedAt(LocalDateTime.now()).build(),
                    LeaveBalance.builder().employee(employee).leaveType(LeaveType.CASUAL).allocationDays(BigDecimal.valueOf(10)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(10)).updatedAt(LocalDateTime.now()).build()
            );
            balances = leaveBalanceRepository.saveAll(balances);
        }
        return ResponseEntity.ok(ApiResponse.<List<LeaveBalance>>builder()
                .success(true).message("Leave balances fetched").data(balances).build());
    }

    /**
     * GET /api/v1/employee/me/dashboard
     * Returns consolidated employee dashboard metrics, compensation, attendance, and granted permissions
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Authentication auth) {
        Employee employee = resolveEmployee(auth);
        Map<String, Object> data = new HashMap<>();

        // Profile (includes basic salary, designation, department)
        data.put("profile", toResponse(employee));

        // Granted Permissions & Roles from user
        try {
            var userProfile = authService.getCurrentUser(auth.getName());
            data.put("permissions", userProfile.getPermissions());
            data.put("roles", userProfile.getRoles());
        } catch (Exception ex) {
            data.put("permissions", Collections.emptySet());
            data.put("roles", Collections.emptySet());
        }

        // Project counts
        try {
            Set<Long> candidateIds = new HashSet<>();
            if (employee.getUser() != null) candidateIds.add(employee.getUser().getId());
            if (employee.getId() != null) candidateIds.add(employee.getId());
            long projectCount = projectMemberRepository.findByUserIdIn(candidateIds).stream()
                    .map(pm -> pm.getProjectId()).distinct().count();
            data.put("projectsCount", projectCount);
        } catch (Exception ex) {
            data.put("projectsCount", 0);
        }

        // Tickets count
        try {
            long openTicketCount = ticketRepository.findByAssignedToId(employee.getId()).stream()
                    .filter(t -> t.getStatus() != null && ("OPEN".equalsIgnoreCase(t.getStatus().name()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus().name())))
                    .count();
            data.put("openTicketsCount", openTicketCount);
        } catch (Exception ex) {
            data.put("openTicketsCount", 0);
        }

        // Worklogs count
        try {
            long workLogCount = workLogRepository.findByEmployeeId(employee.getId(), PageRequest.of(0, 1)).getTotalElements();
            data.put("workLogsCount", workLogCount);
        } catch (Exception ex) {
            data.put("workLogsCount", 0);
        }

        // Today's attendance
        LocalDate today = LocalDate.now();
        Optional<Attendance> todayRecord = attendanceRepository.findByEmployeeIdAndAttendanceDate(employee.getId(), today);
        data.put("todayAttendance", todayRecord.orElse(null));
        String attStatus = todayRecord.map(a -> {
            if (a.getCheckOutTime() != null) return "CLOCKED_OUT";
            if (a.getCheckInTime() != null) return "CLOCKED_IN";
            return a.getStatus() != null ? a.getStatus().name() : "PRESENT";
        }).orElse("NOT_CLOCKED_IN");
        data.put("todayAttendanceStatus", attStatus);

        // Leave balances
        try {
            List<LeaveBalance> balances = leaveBalanceRepository.findByEmployee(employee);
            if (balances.isEmpty()) {
                balances = List.of(
                        LeaveBalance.builder().employee(employee).leaveType(LeaveType.ANNUAL).allocationDays(BigDecimal.valueOf(18)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(18)).updatedAt(LocalDateTime.now()).build(),
                        LeaveBalance.builder().employee(employee).leaveType(LeaveType.SICK).allocationDays(BigDecimal.valueOf(12)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(12)).updatedAt(LocalDateTime.now()).build(),
                        LeaveBalance.builder().employee(employee).leaveType(LeaveType.CASUAL).allocationDays(BigDecimal.valueOf(10)).usedDays(BigDecimal.ZERO).remainingDays(BigDecimal.valueOf(10)).updatedAt(LocalDateTime.now()).build()
                );
                balances = leaveBalanceRepository.saveAll(balances);
            }
            data.put("leaveBalances", balances);
        } catch (Exception ex) {
            data.put("leaveBalances", Collections.emptyList());
        }

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).message("Employee dashboard fetched").data(data).build());
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
