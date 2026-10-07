package com.webliix.hr.worklog.service;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.worklog.dto.WorkLogRequest;
import com.webliix.hr.worklog.dto.WorkLogResponse;
import com.webliix.hr.worklog.dto.WorkLogReviewRequest;
import com.webliix.hr.worklog.entity.WorkLog;
import com.webliix.hr.worklog.repository.WorkLogRepository;
import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.service.NotificationService;
import com.webliix.projects.entity.Project;
import com.webliix.projects.entity.ProjectTask;
import com.webliix.projects.repository.ProjectMemberRepository;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.projects.repository.ProjectTaskRepository;
import com.webliix.security.entity.User;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    private Employee resolveEmployeeFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required.");
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

    @Override
    @Transactional
    public WorkLogResponse submitWorkLog(WorkLogRequest request, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

            // Verify employee is assigned to this project
            boolean isMember = projectMemberRepository.findByProjectId(project.getId())
                    .stream()
                    .anyMatch(m -> m.getUserId() != null && m.getUserId().equals(employee.getUser().getId()));
            if (!isMember) {
                throw new AccessDeniedException("You are not assigned to project: " + project.getProjectName());
            }
        }

        ProjectTask task = null;
        if (request.getTaskId() != null) {
            task = projectTaskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskId()));
        }

        WorkLog workLog = WorkLog.builder()
                .employee(employee)
                .logDate(request.getLogDate() != null ? request.getLogDate() : LocalDate.now())
                .workSummary(request.getWorkSummary())
                .hoursWorked(request.getHoursWorked())
                .project(project)
                .task(task)
                .tasksCompleted(request.getTasksCompleted())
                .blockers(request.getBlockers())
                .status("SUBMITTED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        WorkLog saved = workLogRepository.save(workLog);

        auditService.record(AuditAction.CREATE, AuditModule.HR, "WorkLog",
                saved.getId().toString(), null, saved, "SUCCESS", null);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public WorkLogResponse updateWorkLog(Long id, WorkLogRequest request, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found: " + id));

        if (!workLog.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("You can only update your own work logs.");
        }
        if ("APPROVED".equals(workLog.getStatus())) {
            throw new IllegalStateException("Cannot modify an already approved work log.");
        }

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));
            boolean isMember = projectMemberRepository.findByProjectId(project.getId())
                    .stream()
                    .anyMatch(m -> m.getUserId() != null && m.getUserId().equals(employee.getUser().getId()));
            if (!isMember) {
                throw new AccessDeniedException("You are not assigned to project: " + project.getProjectName());
            }
        }

        ProjectTask task = null;
        if (request.getTaskId() != null) {
            task = projectTaskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTaskId()));
        }

        workLog.setLogDate(request.getLogDate() != null ? request.getLogDate() : workLog.getLogDate());
        workLog.setWorkSummary(request.getWorkSummary());
        workLog.setHoursWorked(request.getHoursWorked());
        workLog.setProject(project);
        workLog.setTask(task);
        workLog.setTasksCompleted(request.getTasksCompleted());
        workLog.setBlockers(request.getBlockers());
        workLog.setStatus("SUBMITTED");
        workLog.setUpdatedAt(LocalDateTime.now());

        WorkLog updated = workLogRepository.save(workLog);

        auditService.record(AuditAction.UPDATE, AuditModule.HR, "WorkLog",
                updated.getId().toString(), null, updated, "SUCCESS", null);

        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkLogResponse getWorkLog(Long id, Authentication auth) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found: " + id));

        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("MANAGER") || a.getAuthority().contains("HR"));
        if (!isStaff) {
            Employee employee = resolveEmployeeFromAuth(auth);
            if (!workLog.getEmployee().getId().equals(employee.getId())) {
                throw new AccessDeniedException("Access denied.");
            }
        }
        return toResponse(workLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkLogResponse> getMyWorkLogs(Pageable pageable, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);
        return workLogRepository.findByEmployeeId(employee.getId(), pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkLogResponse> getMyWorkLogsByDateRange(LocalDate from, LocalDate to, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);
        return workLogRepository.findByEmployeeIdAndLogDateBetweenOrderByLogDateDesc(employee.getId(), from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkLogResponse> getAllWorkLogs(Pageable pageable) {
        return workLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkLogResponse> getWorkLogsByEmployee(Long employeeId, Pageable pageable) {
        return workLogRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public WorkLogResponse reviewWorkLog(Long id, WorkLogReviewRequest request, Authentication auth) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found: " + id));

        if (!"APPROVED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        workLog.setStatus(request.getStatus());
        workLog.setReviewedBy(auth != null ? auth.getName() : "System");
        workLog.setReviewNotes(request.getReviewNotes());
        workLog.setReviewedAt(LocalDateTime.now());
        workLog.setUpdatedAt(LocalDateTime.now());

        WorkLog saved = workLogRepository.save(workLog);

        auditService.record(AuditAction.APPROVE, AuditModule.HR, "WorkLog",
                saved.getId().toString(), null, saved, request.getStatus(), null);

        // Notify employee about review status
        try {
            if (workLog.getEmployee() != null && workLog.getEmployee().getEmail() != null) {
                CreateNotificationRequest notif = new CreateNotificationRequest();
                notif.setTitle("Daily Work Log " + request.getStatus());
                notif.setMessage("Your work log for " + workLog.getLogDate() + " has been " + request.getStatus().toLowerCase() +
                        (request.getReviewNotes() != null ? ": " + request.getReviewNotes() : "."));
                notif.setRecipient(workLog.getEmployee().getEmail());
                notif.setRecipientType("USER");
                notif.setChannel(NotificationChannel.IN_APP);
                notif.setReferenceType("WORK_LOG");
                notif.setReferenceId(workLog.getId());
                notificationService.createNotification(notif);
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for work log review: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteWorkLog(Long id, Authentication auth) {
        WorkLog workLog = workLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work log not found: " + id));

        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("MANAGER"));
        if (!isStaff) {
            Employee employee = resolveEmployeeFromAuth(auth);
            if (!workLog.getEmployee().getId().equals(employee.getId())) {
                throw new AccessDeniedException("You can only delete your own work logs.");
            }
        }

        auditService.record(AuditAction.DELETE, AuditModule.HR, "WorkLog",
                id.toString(), workLog, null, "SUCCESS", null);

        workLogRepository.deleteById(id);
    }

    private WorkLogResponse toResponse(WorkLog log) {
        WorkLogResponse res = new WorkLogResponse();
        res.setId(log.getId());
        if (log.getEmployee() != null) {
            res.setEmployeeId(log.getEmployee().getId());
            res.setEmployeeName(log.getEmployee().getFirstName() + " " + log.getEmployee().getLastName());
            res.setEmployeeCode(log.getEmployee().getEmployeeCode());
        }
        res.setLogDate(log.getLogDate());
        res.setWorkSummary(log.getWorkSummary());
        res.setHoursWorked(log.getHoursWorked());
        if (log.getProject() != null) {
            res.setProjectId(log.getProject().getId());
            res.setProjectName(log.getProject().getProjectName());
        }
        if (log.getTask() != null) {
            res.setTaskId(log.getTask().getId());
            res.setTaskTitle(log.getTask().getTitle());
        }
        res.setTasksCompleted(log.getTasksCompleted());
        res.setBlockers(log.getBlockers());
        res.setStatus(log.getStatus());
        res.setReviewedBy(log.getReviewedBy());
        res.setReviewNotes(log.getReviewNotes());
        res.setReviewedAt(log.getReviewedAt());
        res.setCreatedAt(log.getCreatedAt());
        res.setUpdatedAt(log.getUpdatedAt());
        return res;
    }
}
