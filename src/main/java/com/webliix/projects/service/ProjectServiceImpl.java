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
        return ProjectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse getProject(Long id) {
        Project project = findProjectById(id);
        return ProjectMapper.toResponse(project);
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
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
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable) {
        return projectRepository.findByProjectNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable)
                .map(ProjectMapper::toResponse);
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

