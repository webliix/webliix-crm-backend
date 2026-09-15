package com.webliix.projects.service;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.projects.dto.*;
import com.webliix.projects.entity.*;
import com.webliix.projects.enums.ProjectTaskStatus;

import java.time.LocalDateTime;

public class ProjectMapper {

    public static Project toEntity(CreateProjectRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setBudget(request.getBudget());
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        project.setActualEndDate(request.getActualEndDate());
        project.setStatus(request.getStatus() != null ? request.getStatus() : com.webliix.projects.enums.ProjectStatus.PLANNING);
        project.setPriority(request.getPriority() != null ? request.getPriority() : com.webliix.projects.enums.ProjectPriority.MEDIUM);
        project.setBillable(request.getBillable() != null ? request.getBillable() : false);
        project.setProgressPercentage(0);
        return project;
    }

    public static ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectCode(project.getProjectCode());
        response.setProjectName(project.getProjectName());
        response.setDescription(project.getDescription());
        response.setBudget(project.getBudget());
        response.setStartDate(project.getStartDate());
        response.setExpectedEndDate(project.getExpectedEndDate());
        response.setActualEndDate(project.getActualEndDate());
        response.setStatus(project.getStatus());
        response.setPriority(project.getPriority());
        response.setCustomerId(project.getCustomer() != null ? project.getCustomer().getId() : null);
        response.setProgressPercentage(project.getProgressPercentage());
        response.setBillable(project.getBillable());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }

    public static void updateEntity(Project project, CreateProjectRequest request) {
        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getExpectedEndDate() != null) {
            project.setExpectedEndDate(request.getExpectedEndDate());
        }
        if (request.getActualEndDate() != null) {
            project.setActualEndDate(request.getActualEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getBillable() != null) {
            project.setBillable(request.getBillable());
        }
    }

    public static ProjectMember toEntity(CreateProjectMemberRequest request) {
        ProjectMember member = new ProjectMember();
        member.setUserId(request.getUserId());
        member.setRoleInProject(request.getRoleInProject());
        member.setAssignedDate(request.getAssignedDate());
        return member;
    }

    public static ProjectMemberResponse toResponse(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.setId(member.getId());
        response.setProjectId(member.getProjectId());
        response.setUserId(member.getUserId());
        response.setRoleInProject(member.getRoleInProject());
        response.setAssignedDate(member.getAssignedDate());
        return response;
    }

    public static ProjectMilestone toEntity(CreateProjectMilestoneRequest request) {
        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        milestone.setCompleted(request.getCompleted() != null ? request.getCompleted() : false);
        milestone.setCompletedAt(request.getCompleted() != null && request.getCompleted() ? LocalDateTime.now() : null);
        return milestone;
    }

    public static ProjectMilestoneResponse toResponse(ProjectMilestone milestone) {
        ProjectMilestoneResponse response = new ProjectMilestoneResponse();
        response.setId(milestone.getId());
        response.setProjectId(milestone.getProjectId());
        response.setTitle(milestone.getTitle());
        response.setDescription(milestone.getDescription());
        response.setDueDate(milestone.getDueDate());
        response.setCompleted(milestone.getCompleted());
        response.setCompletedAt(milestone.getCompletedAt());
        return response;
    }

    public static void updateEntity(ProjectMilestone milestone, CreateProjectMilestoneRequest request) {
        if (request.getTitle() != null) {
            milestone.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            milestone.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            milestone.setDueDate(request.getDueDate());
        }
        if (request.getCompleted() != null) {
            milestone.setCompleted(request.getCompleted());
            milestone.setCompletedAt(request.getCompleted() ? LocalDateTime.now() : null);
        }
    }

    public static ProjectTask toEntity(CreateProjectTaskRequest request) {
        ProjectTask task = new ProjectTask();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : ProjectTaskStatus.TODO);
        task.setAssignedTo(request.getAssignedTo());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setCompletedAt(request.getStatus() == ProjectTaskStatus.DONE ? LocalDateTime.now() : null);
        return task;
    }

    public static ProjectTaskResponse toResponse(ProjectTask task) {
        ProjectTaskResponse response = new ProjectTaskResponse();
        response.setId(task.getId());
        response.setProjectId(task.getProjectId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setAssignedTo(task.getAssignedTo());
        response.setStartDate(task.getStartDate());
        response.setDueDate(task.getDueDate());
        response.setCompletedAt(task.getCompletedAt());
        return response;
    }

    public static void updateEntity(ProjectTask task, CreateProjectTaskRequest request) {
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            task.setCompletedAt(request.getStatus() == ProjectTaskStatus.DONE ? LocalDateTime.now() : null);
        }
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
    }

    public static ProjectComment toEntity(CreateProjectCommentRequest request) {
        ProjectComment comment = new ProjectComment();
        comment.setAuthorId(request.getAuthorId());
        comment.setMessage(request.getMessage());
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    public static ProjectCommentResponse toResponse(ProjectComment comment) {
        ProjectCommentResponse response = new ProjectCommentResponse();
        response.setId(comment.getId());
        response.setProjectId(comment.getProjectId());
        response.setTaskId(comment.getTaskId());
        response.setAuthorId(comment.getAuthorId());
        response.setMessage(comment.getMessage());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}

