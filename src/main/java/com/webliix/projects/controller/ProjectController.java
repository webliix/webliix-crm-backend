package com.webliix.projects.controller;

import com.webliix.projects.dto.*;
import com.webliix.projects.service.ProjectService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> response = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProjectResponse>>builder()
                .success(true)
                .message("Projects fetched")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> response = projectService.searchProjects(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProjectResponse>>builder()
                .success(true)
                .message("Project search results")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        ProjectResponse response = projectService.getProject(id);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable Long id, @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project deleted successfully")
                .build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ProjectDashboardResponse>> getDashboard() {
        ProjectDashboardResponse response = projectService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<ProjectDashboardResponse>builder()
                .success(true)
                .message("Project dashboard fetched")
                .data(response)
                .build());
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(@PathVariable Long projectId,
                                                                              @RequestBody CreateProjectMemberRequest request) {
        ProjectMemberResponse response = projectService.addProjectMember(projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectMemberResponse>builder()
                .success(true)
                .message("Project member added")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponse> response = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectMemberResponse>>builder()
                .success(true)
                .message("Project members fetched")
                .data(response)
                .build());
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(@PathVariable Long projectId,
                                                                 @PathVariable Long memberId) {
        projectService.removeProjectMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project member removed")
                .build());
    }

    @PostMapping("/{projectId}/milestones")
    public ResponseEntity<ApiResponse<ProjectMilestoneResponse>> addProjectMilestone(@PathVariable Long projectId,
                                                                                    @RequestBody CreateProjectMilestoneRequest request) {
        ProjectMilestoneResponse response = projectService.addProjectMilestone(projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectMilestoneResponse>builder()
                .success(true)
                .message("Project milestone created")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/milestones")
    public ResponseEntity<ApiResponse<List<ProjectMilestoneResponse>>> getProjectMilestones(@PathVariable Long projectId) {
        List<ProjectMilestoneResponse> response = projectService.getProjectMilestones(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectMilestoneResponse>>builder()
                .success(true)
                .message("Project milestones fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<ProjectMilestoneResponse>> updateProjectMilestone(@PathVariable Long projectId,
                                                                                       @PathVariable Long milestoneId,
                                                                                       @RequestBody CreateProjectMilestoneRequest request) {
        ProjectMilestoneResponse response = projectService.updateProjectMilestone(projectId, milestoneId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectMilestoneResponse>builder()
                .success(true)
                .message("Project milestone updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<Void>> deleteProjectMilestone(@PathVariable Long projectId,
                                                                    @PathVariable Long milestoneId) {
        projectService.deleteProjectMilestone(projectId, milestoneId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project milestone deleted")
                .build());
    }

    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<ApiResponse<ProjectTaskResponse>> addProjectTask(@PathVariable Long projectId,
                                                                           @RequestBody CreateProjectTaskRequest request) {
        ProjectTaskResponse response = projectService.addProjectTask(projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectTaskResponse>builder()
                .success(true)
                .message("Project task created")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> getProjectTasks(@PathVariable Long projectId) {
        List<ProjectTaskResponse> response = projectService.getProjectTasks(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectTaskResponse>>builder()
                .success(true)
                .message("Project tasks fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<ProjectTaskResponse>> updateProjectTask(@PathVariable Long projectId,
                                                                              @PathVariable Long taskId,
                                                                              @RequestBody CreateProjectTaskRequest request) {
        ProjectTaskResponse response = projectService.updateProjectTask(projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectTaskResponse>builder()
                .success(true)
                .message("Project task updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteProjectTask(@PathVariable Long projectId,
                                                               @PathVariable Long taskId) {
        projectService.deleteProjectTask(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project task deleted")
                .build());
    }

    @PostMapping("/{projectId}/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<ProjectCommentResponse>> addProjectComment(@PathVariable Long projectId,
                                                                                 @PathVariable Long taskId,
                                                                                 @RequestBody CreateProjectCommentRequest request) {
        ProjectCommentResponse response = projectService.addProjectComment(projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectCommentResponse>builder()
                .success(true)
                .message("Comment added to task")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<ProjectCommentResponse>>> getProjectComments(@PathVariable Long projectId,
                                                                                         @PathVariable Long taskId) {
        List<ProjectCommentResponse> response = projectService.getProjectComments(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectCommentResponse>>builder()
                .success(true)
                .message("Task comments fetched")
                .data(response)
                .build());
    }
}

