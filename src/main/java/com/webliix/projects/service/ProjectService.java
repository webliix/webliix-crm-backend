package com.webliix.projects.service;

import com.webliix.projects.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProject(Long id, Authentication auth);

    Page<ProjectResponse> getAllProjects(Pageable pageable, Authentication auth);

    Page<ProjectResponse> getAllProjects(Long customerId, Pageable pageable, Authentication auth);

    ProjectResponse updateProject(Long id, CreateProjectRequest request);

    void deleteProject(Long id);

    Page<ProjectResponse> searchProjects(String keyword, Pageable pageable, Authentication auth);

    ProjectMemberResponse addProjectMember(Long projectId, CreateProjectMemberRequest request);

    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    void removeProjectMember(Long projectId, Long memberId);

    ProjectMilestoneResponse addProjectMilestone(Long projectId, CreateProjectMilestoneRequest request);

    List<ProjectMilestoneResponse> getProjectMilestones(Long projectId);

    ProjectMilestoneResponse updateProjectMilestone(Long projectId, Long milestoneId, CreateProjectMilestoneRequest request);

    void deleteProjectMilestone(Long projectId, Long milestoneId);

    ProjectTaskResponse addProjectTask(Long projectId, CreateProjectTaskRequest request);

    List<ProjectTaskResponse> getProjectTasks(Long projectId);

    ProjectTaskResponse updateProjectTask(Long projectId, Long taskId, CreateProjectTaskRequest request);

    void deleteProjectTask(Long projectId, Long taskId);

    ProjectCommentResponse addProjectComment(Long projectId, Long taskId, CreateProjectCommentRequest request);

    List<ProjectCommentResponse> getProjectComments(Long projectId, Long taskId);

    ProjectCommentResponse addProjectInstructionOrUpdate(Long projectId, CreateProjectCommentRequest request, Authentication auth);

    List<ProjectCommentResponse> getProjectInstructionsAndUpdates(Long projectId);

    ProjectResponse updateProjectProgress(Long projectId, Integer progressPercentage, com.webliix.projects.enums.ProjectStatus status, String updateNote);

    ProjectDashboardResponse getDashboard();
}

