package com.webliix.projects.controller;

import com.webliix.projects.dto.*;
import com.webliix.projects.service.ProjectService;
import com.webliix.security.annotation.RequirePermission;
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
    private final com.webliix.hr.worklog.service.WorkLogService workLogService;

    @PostMapping
    @RequirePermission("PROJECTS_CREATE")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjects(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            org.springframework.security.core.Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> response = projectService.getAllProjects(customerId, pageable, authentication);
        return ResponseEntity.ok(ApiResponse.<Page<ProjectResponse>>builder()
                .success(true)
                .message("Projects fetched")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @RequirePermission("PROJECTS_VIEW")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            org.springframework.security.core.Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> response = projectService.searchProjects(keyword, pageable, authentication);
        return ResponseEntity.ok(ApiResponse.<Page<ProjectResponse>>builder()
                .success(true)
                .message("Project search results")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication
    ) {
        ProjectResponse response = projectService.getProject(id, authentication);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @RequirePermission("PROJECTS_EDIT")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable Long id, @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("PROJECTS_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project deleted successfully")
                .build());
    }

    @GetMapping("/dashboard")
    @RequirePermission("PROJECTS_VIEW")
    public ResponseEntity<ApiResponse<ProjectDashboardResponse>> getDashboard() {
        ProjectDashboardResponse response = projectService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<ProjectDashboardResponse>builder()
                .success(true)
                .message("Project dashboard fetched")
                .data(response)
                .build());
    }

    @PostMapping("/{projectId}/members")
    @RequirePermission("PROJECTS_EDIT")
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
    @RequirePermission("PROJECTS_VIEW")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponse> response = projectService.getProjectMembers(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectMemberResponse>>builder()
                .success(true)
                .message("Project members fetched")
                .data(response)
                .build());
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @RequirePermission("PROJECTS_EDIT")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(@PathVariable Long projectId,
                                                                 @PathVariable Long memberId) {
        projectService.removeProjectMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project member removed")
                .build());
    }

    @PostMapping("/{projectId}/milestones")
    @RequirePermission("PROJECTS_EDIT")
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
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<ProjectMilestoneResponse>>> getProjectMilestones(@PathVariable Long projectId) {
        List<ProjectMilestoneResponse> response = projectService.getProjectMilestones(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectMilestoneResponse>>builder()
                .success(true)
                .message("Project milestones fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{projectId}/milestones/{milestoneId}")
    @RequirePermission("PROJECTS_EDIT")
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
    @RequirePermission("PROJECTS_EDIT")
    public ResponseEntity<ApiResponse<Void>> deleteProjectMilestone(@PathVariable Long projectId,
                                                                    @PathVariable Long milestoneId) {
        projectService.deleteProjectMilestone(projectId, milestoneId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project milestone deleted")
                .build());
    }

    @PostMapping("/{projectId}/tasks")
    @RequirePermission("PROJECTS_EDIT")
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
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> getProjectTasks(@PathVariable Long projectId) {
        List<ProjectTaskResponse> response = projectService.getProjectTasks(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectTaskResponse>>builder()
                .success(true)
                .message("Project tasks fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{projectId}/tasks/{taskId}")
    @RequirePermission("PROJECTS_EDIT")
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
    @RequirePermission("PROJECTS_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteProjectTask(@PathVariable Long projectId,
                                                               @PathVariable Long taskId) {
        projectService.deleteProjectTask(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Project task deleted")
                .build());
    }

    @PostMapping("/{projectId}/tasks/{taskId}/comments")
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
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
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<ProjectCommentResponse>>> getProjectComments(@PathVariable Long projectId,
                                                                                         @PathVariable Long taskId) {
        List<ProjectCommentResponse> response = projectService.getProjectComments(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectCommentResponse>>builder()
                .success(true)
                .message("Task comments fetched")
                .data(response)
                .build());
    }

    @PostMapping("/{projectId}/comments")
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<ProjectCommentResponse>> addProjectInstructionOrUpdate(
            @PathVariable Long projectId,
            @RequestBody CreateProjectCommentRequest request,
            org.springframework.security.core.Authentication authentication
    ) {
        ProjectCommentResponse response = projectService.addProjectInstructionOrUpdate(projectId, request, authentication);
        return ResponseEntity.ok(ApiResponse.<ProjectCommentResponse>builder()
                .success(true)
                .message("Project update / client instruction submitted successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/comments")
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<ProjectCommentResponse>>> getProjectInstructionsAndUpdates(@PathVariable Long projectId) {
        List<ProjectCommentResponse> response = projectService.getProjectInstructionsAndUpdates(projectId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectCommentResponse>>builder()
                .success(true)
                .message("Project updates and instructions fetched")
                .data(response)
                .build());
    }

    @PatchMapping("/{projectId}/progress")
    @RequirePermission("PROJECTS_EDIT")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectProgress(
            @PathVariable Long projectId,
            @RequestParam(required = false) Integer progressPercentage,
            @RequestParam(required = false) com.webliix.projects.enums.ProjectStatus status,
            @RequestParam(required = false) String updateNote
    ) {
        ProjectResponse response = projectService.updateProjectProgress(projectId, progressPercentage, status, updateNote);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project progress and status updated")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/billing")
    @RequirePermission(anyOf = {"PROJECTS_BILLING_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<ProjectBillingResponse>> getProjectBilling(
            @PathVariable Long projectId,
            org.springframework.security.core.Authentication authentication
    ) {
        ProjectBillingResponse response = projectService.getProjectBilling(projectId, authentication);
        return ResponseEntity.ok(ApiResponse.<ProjectBillingResponse>builder()
                .success(true)
                .message("Project billing summary fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/invoices")
    @RequirePermission(anyOf = {"PROJECTS_BILLING_VIEW", "INVOICES_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<com.webliix.finance.dto.InvoiceResponse>>> getProjectInvoices(
            @PathVariable Long projectId,
            org.springframework.security.core.Authentication authentication
    ) {
        List<com.webliix.finance.dto.InvoiceResponse> response = projectService.getProjectInvoices(projectId, authentication);
        return ResponseEntity.ok(ApiResponse.<List<com.webliix.finance.dto.InvoiceResponse>>builder()
                .success(true)
                .message("Project invoices fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{projectId}/work-logs")
    @RequirePermission(anyOf = {"PROJECTS_VIEW", "WORKLOGS_VIEW"})
    public ResponseEntity<ApiResponse<List<com.webliix.hr.worklog.dto.WorkLogResponse>>> getProjectWorkLogs(
            @PathVariable Long projectId,
            org.springframework.security.core.Authentication authentication
    ) {
        List<com.webliix.hr.worklog.dto.WorkLogResponse> response = workLogService.getWorkLogsByProject(projectId, authentication);
        return ResponseEntity.ok(ApiResponse.<List<com.webliix.hr.worklog.dto.WorkLogResponse>>builder()
                .success(true)
                .message("Project work logs and task updates fetched")
                .data(response)
                .build());
    }
}
