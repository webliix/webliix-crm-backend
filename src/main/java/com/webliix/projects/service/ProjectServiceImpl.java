package com.webliix.projects.service;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.projects.dto.*;
import com.webliix.projects.entity.*;
import com.webliix.projects.enums.ProjectStatus;
import com.webliix.projects.enums.ProjectTaskStatus;
import com.webliix.projects.repository.*;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectMilestoneRepository milestoneRepository;
    private final ProjectTaskRepository taskRepository;
    private final ProjectCommentRepository commentRepository;
    private final CustomerRepository customerRepository;
    private final ProjectCodeGenerator codeGenerator;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        Project project = ProjectMapper.toEntity(request);
        project.setProjectCode(codeGenerator.generateNextProjectCode());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setProgressPercentage(0);

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            project.setCustomer(customer);
        }

        Project saved = projectRepository.save(project);

        if (request.getAutoGeneratePhases() == null || Boolean.TRUE.equals(request.getAutoGeneratePhases())) {
            generateDefaultPhases(saved);
        }

        if (saved.getCustomer() != null && saved.getCustomer().getEmail() != null) {
            eventPublisher.publishEvent(new com.webliix.notifications.event.ProjectCreatedEvent(
                    this,
                    saved.getId(),
                    saved.getProjectName(),
                    saved.getCustomer().getId(),
                    saved.getCustomer().getEmail()
            ));
        }

        return ProjectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse getProject(Long id, org.springframework.security.core.Authentication auth) {
        Project project = findProjectById(id);
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            if (project.getCustomer() == null || project.getCustomer().getEmail() == null || !project.getCustomer().getEmail().equalsIgnoreCase(email)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only view your own customer projects.");
            }
        }
        return ProjectMapper.toResponse(project);
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable, org.springframework.security.core.Authentication auth) {
        return getAllProjects(null, pageable, auth);
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Long customerId, Pageable pageable, org.springframework.security.core.Authentication auth) {
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            return projectRepository.findByCustomerEmail(email, pageable).map(ProjectMapper::toResponse);
        }
        if (customerId != null) {
            return projectRepository.findByCustomerId(customerId, pageable).map(ProjectMapper::toResponse);
        }
        return projectRepository.findAll(pageable).map(ProjectMapper::toResponse);
    }

    @Override
    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = findProjectById(id);
        ProjectMapper.updateEntity(project, request);
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            project.setCustomer(customer);
        }
        project.setUpdatedAt(LocalDateTime.now());
        Project updated = projectRepository.save(project);
        return ProjectMapper.toResponse(updated);
    }

    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        deleteCommentsByProject(id);
        deleteTasksByProject(id);
        deleteMilestonesByProject(id);
        deleteMembersByProject(id);
        projectRepository.deleteById(id);
    }

    @Override
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable, org.springframework.security.core.Authentication auth) {
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            return projectRepository.findByCustomerEmailWithSearch(email, keyword, pageable)
                    .map(ProjectMapper::toResponse);
        }
        return projectRepository.findByProjectNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable)
                .map(ProjectMapper::toResponse);
    }

    private boolean isCustomer(org.springframework.security.core.Authentication auth) {
        if (auth == null) return false;
        boolean isStaff = auth.getAuthorities().stream().anyMatch(a -> {
            String authName = a.getAuthority();
            return "ROLE_ADMIN".equals(authName) || "ADMIN".equals(authName)
                    || "ROLE_SUPER_ADMIN".equals(authName) || "SUPER_ADMIN".equals(authName)
                    || "ROLE_MANAGER".equals(authName) || "MANAGER".equals(authName)
                    || "ROLE_EMPLOYEE".equals(authName) || "EMPLOYEE".equals(authName);
        });
        if (isStaff) return false;
        return auth.getAuthorities().stream().anyMatch(a -> 
            "ROLE_USER".equals(a.getAuthority()) || "USER".equals(a.getAuthority())
            || "ROLE_CUSTOMER".equals(a.getAuthority()) || "CUSTOMER".equals(a.getAuthority())
        );
    }

    @Override
    public ProjectMemberResponse addProjectMember(Long projectId, CreateProjectMemberRequest request) {
        Project project = findProjectById(projectId);
        ProjectMember member = ProjectMapper.toEntity(request);
        member.setProjectId(project.getId());
        ProjectMember saved = memberRepository.save(member);
        return ProjectMapper.toResponse(saved);
    }

    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        findProjectById(projectId);
        return memberRepository.findByProjectId(projectId).stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId) {
        ProjectMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found with id: " + memberId));
        if (!member.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Project member does not belong to project id: " + projectId);
        }
        memberRepository.delete(member);
    }

    @Override
    public ProjectMilestoneResponse addProjectMilestone(Long projectId, CreateProjectMilestoneRequest request) {
        Project project = findProjectById(projectId);
        ProjectMilestone milestone = ProjectMapper.toEntity(request);
        milestone.setProjectId(project.getId());
        ProjectMilestone saved = milestoneRepository.save(milestone);
        return ProjectMapper.toResponse(saved);
    }

    @Override
    public List<ProjectMilestoneResponse> getProjectMilestones(Long projectId) {
        findProjectById(projectId);
        return milestoneRepository.findByProjectId(projectId).stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectMilestoneResponse updateProjectMilestone(Long projectId, Long milestoneId, CreateProjectMilestoneRequest request) {
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));
        if (!milestone.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Milestone does not belong to project id: " + projectId);
        }
        ProjectMapper.updateEntity(milestone, request);
        ProjectMilestone updated = milestoneRepository.save(milestone);
        return ProjectMapper.toResponse(updated);
    }

    @Override
    public void deleteProjectMilestone(Long projectId, Long milestoneId) {
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + milestoneId));
        if (!milestone.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Milestone does not belong to project id: " + projectId);
        }
        milestoneRepository.delete(milestone);
    }

    @Override
    public ProjectTaskResponse addProjectTask(Long projectId, CreateProjectTaskRequest request) {
        Project project = findProjectById(projectId);
        ProjectTask task = ProjectMapper.toEntity(request);
        task.setProjectId(project.getId());
        ProjectTask saved = taskRepository.save(task);
        recalculateProgress(project);
        return ProjectMapper.toResponse(saved);
    }

    @Override
    public List<ProjectTaskResponse> getProjectTasks(Long projectId) {
        findProjectById(projectId);
        return taskRepository.findByProjectId(projectId).stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectTaskResponse updateProjectTask(Long projectId, Long taskId, CreateProjectTaskRequest request) {
        Project project = findProjectById(projectId);
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task does not belong to project id: " + projectId);
        }
        ProjectMapper.updateEntity(task, request);
        ProjectTask updated = taskRepository.save(task);
        recalculateProgress(project);
        return ProjectMapper.toResponse(updated);
    }

    @Override
    public void deleteProjectTask(Long projectId, Long taskId) {
        Project project = findProjectById(projectId);
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task does not belong to project id: " + projectId);
        }
        taskRepository.delete(task);
        recalculateProgress(project);
    }

    @Override
    public ProjectCommentResponse addProjectComment(Long projectId, Long taskId, CreateProjectCommentRequest request) {
        findProjectById(projectId);
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task does not belong to project id: " + projectId);
        }
        ProjectComment comment = ProjectMapper.toEntity(request);
        comment.setProjectId(projectId);
        comment.setTaskId(taskId);
        ProjectComment saved = commentRepository.save(comment);
        return ProjectMapper.toResponse(saved);
    }

    @Override
    public List<ProjectCommentResponse> getProjectComments(Long projectId, Long taskId) {
        findProjectById(projectId);
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Task does not belong to project id: " + projectId);
        }
        return commentRepository.findByProjectIdAndTaskId(projectId, taskId).stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectCommentResponse addProjectInstructionOrUpdate(Long projectId, CreateProjectCommentRequest request, org.springframework.security.core.Authentication auth) {
        Project project = findProjectById(projectId);
        ProjectComment comment = ProjectMapper.toEntity(request);
        comment.setProjectId(projectId);

        boolean isCust = isCustomer(auth) || "CLIENT".equalsIgnoreCase(request.getAuthorRole());
        if (isCust) {
            comment.setAuthorRole("CLIENT");
            if (comment.getAuthorName() == null || comment.getAuthorName().isEmpty()) {
                comment.setAuthorName(project.getCustomer() != null ? project.getCustomer().getContactPerson() : "Client");
            }
        } else {
            comment.setAuthorRole("ADMIN");
            if (comment.getAuthorName() == null || comment.getAuthorName().isEmpty()) {
                comment.setAuthorName("Webliix Project Lead");
            }
        }

        ProjectComment saved = commentRepository.save(comment);

        if ("ADMIN".equalsIgnoreCase(saved.getAuthorRole()) && project.getCustomer() != null && project.getCustomer().getEmail() != null) {
            eventPublisher.publishEvent(new com.webliix.notifications.event.ProjectUpdateEvent(
                    this,
                    project.getId(),
                    project.getProjectName(),
                    project.getCustomer().getEmail(),
                    "New Project Update: " + project.getProjectName(),
                    request.getMessage(),
                    project.getProgressPercentage(),
                    project.getStatus() != null ? project.getStatus().name() : "IN_PROGRESS"
            ));
        }

        return ProjectMapper.toResponse(saved);
    }

    @Override
    public List<ProjectCommentResponse> getProjectInstructionsAndUpdates(Long projectId) {
        findProjectById(projectId);
        return commentRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse updateProjectProgress(Long projectId, Integer progressPercentage, ProjectStatus status, String updateNote) {
        Project project = findProjectById(projectId);
        if (progressPercentage != null) {
            project.setProgressPercentage(progressPercentage);
        }
        if (status != null) {
            project.setStatus(status);
        }
        project.setUpdatedAt(LocalDateTime.now());
        Project updated = projectRepository.save(project);

        if (updateNote != null && !updateNote.trim().isEmpty()) {
            ProjectComment updateComment = ProjectComment.builder()
                    .projectId(projectId)
                    .authorName("Super Admin / Project Lead")
                    .authorRole("ADMIN")
                    .message("Progress Updated to " + (progressPercentage != null ? progressPercentage : project.getProgressPercentage()) + "%: " + updateNote)
                    .createdAt(LocalDateTime.now())
                    .build();
            commentRepository.save(updateComment);
        }

        if (updated.getCustomer() != null && updated.getCustomer().getEmail() != null) {
            eventPublisher.publishEvent(new com.webliix.notifications.event.ProjectUpdateEvent(
                    this,
                    updated.getId(),
                    updated.getProjectName(),
                    updated.getCustomer().getEmail(),
                    "Project Status & Progress Updated: " + updated.getProjectName(),
                    updateNote != null ? updateNote : ("Project progress is now " + updated.getProgressPercentage() + "% (" + updated.getStatus() + ")."),
                    updated.getProgressPercentage(),
                    updated.getStatus() != null ? updated.getStatus().name() : "IN_PROGRESS"
            ));
        }

        return ProjectMapper.toResponse(updated);
    }

    private void generateDefaultPhases(Project project) {
        LocalDate start = project.getStartDate() != null ? project.getStartDate() : LocalDate.now();
        LocalDate end = project.getExpectedEndDate() != null ? project.getExpectedEndDate() : start.plusDays(45);
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (totalDays <= 0) totalDays = 45;

        createMilestoneHelper(project.getId(), "Phase 1: Discovery & Architecture Blueprint", 
                "System requirements gathering, data schema definition, and API contract design.", start.plusDays((long) (totalDays * 0.15)));
        createMilestoneHelper(project.getId(), "Phase 2: UI/UX & Interactive Prototyping", 
                "Wireframing, design system layout, and frontend component architecture.", start.plusDays((long) (totalDays * 0.35)));
        createMilestoneHelper(project.getId(), "Phase 3: Core Engineering & Backend Integration", 
                "Database migrations, core business services, REST endpoints, and security layer.", start.plusDays((long) (totalDays * 0.70)));
        createMilestoneHelper(project.getId(), "Phase 4: Quality Assurance & Security Hardening", 
                "End-to-end integration testing, performance optimization, and vulnerability scans.", start.plusDays((long) (totalDays * 0.90)));
        createMilestoneHelper(project.getId(), "Phase 5: Production Deployment & Client Handover", 
                "Cloud infrastructure provisioning, domain & SSL mapping, documentation, and user onboarding.", end);
    }

    private void createMilestoneHelper(Long projectId, String title, String description, LocalDate dueDate) {
        ProjectMilestone milestone = ProjectMilestone.builder()
                .projectId(projectId)
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .completed(false)
                .build();
        milestoneRepository.save(milestone);
    }

    @Override
    public ProjectDashboardResponse getDashboard() {
        ProjectDashboardResponse response = new ProjectDashboardResponse();
        response.setTotalProjects(projectRepository.count());
        response.setPlanningProjects(projectRepository.countByStatus(ProjectStatus.PLANNING));
        response.setInProgressProjects(projectRepository.countByStatus(ProjectStatus.IN_PROGRESS));
        response.setCompletedProjects(projectRepository.countByStatus(ProjectStatus.COMPLETED));
        response.setCancelledProjects(projectRepository.countByStatus(ProjectStatus.CANCELLED));
        response.setOverdueProjects(projectRepository.countByExpectedEndDateBeforeAndStatusNot(LocalDate.now(), ProjectStatus.COMPLETED));
        response.setTotalTasks(taskRepository.count());
        response.setCompletedTasks(taskRepository.countByStatus(ProjectTaskStatus.DONE));
        response.setOverdueTasks(taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), ProjectTaskStatus.DONE));
        response.setActiveMilestones(milestoneRepository.countByDueDateBeforeAndCompletedFalse(LocalDate.now()));
        return response;
    }

    private Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private void recalculateProgress(Project project) {
        long totalTasks = taskRepository.countByProjectId(project.getId());
        if (totalTasks == 0) {
            project.setProgressPercentage(0);
            projectRepository.save(project);
            return;
        }
        long completed = taskRepository.countByProjectIdAndStatus(project.getId(), ProjectTaskStatus.DONE);
        int progress = (int) ((completed * 100) / totalTasks);
        project.setProgressPercentage(progress);
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    private void deleteTasksByProject(Long projectId) {
        List<ProjectTask> tasks = taskRepository.findByProjectId(projectId);
        taskRepository.deleteAll(tasks);
    }

    private void deleteMilestonesByProject(Long projectId) {
        List<ProjectMilestone> milestones = milestoneRepository.findByProjectId(projectId);
        milestoneRepository.deleteAll(milestones);
    }

    private void deleteMembersByProject(Long projectId) {
        List<ProjectMember> members = memberRepository.findByProjectId(projectId);
        memberRepository.deleteAll(members);
    }

    private void deleteCommentsByProject(Long projectId) {
        List<ProjectComment> comments = commentRepository.findByProjectId(projectId);
        commentRepository.deleteAll(comments);
    }
}

