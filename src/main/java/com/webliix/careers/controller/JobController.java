package com.webliix.careers.controller;

import com.webliix.careers.dto.*;
import com.webliix.careers.service.JobService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // =========================================================================
    // JOB OPENINGS
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAnyAuthority('JOBS_VIEW', 'JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission(anyOf = {"JOBS_VIEW", "JOBS_MANAGE"})
    public ResponseEntity<ApiResponse<List<JobOpeningDto>>> getAllOpenings() {
        List<JobOpeningDto> list = jobService.getAllOpenings();
        return ResponseEntity.ok(ApiResponse.<List<JobOpeningDto>>builder()
                .success(true)
                .message("Job openings retrieved successfully")
                .data(list)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('JOBS_VIEW', 'JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission(anyOf = {"JOBS_VIEW", "JOBS_MANAGE"})
    public ResponseEntity<ApiResponse<JobOpeningDto>> getOpeningById(@PathVariable Long id) {
        JobOpeningDto dto = jobService.getOpeningById(id);
        return ResponseEntity.ok(ApiResponse.<JobOpeningDto>builder()
                .success(true)
                .message("Job opening retrieved successfully")
                .data(dto)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission("JOBS_MANAGE")
    public ResponseEntity<ApiResponse<JobOpeningDto>> createOpening(@Valid @RequestBody CreateJobOpeningRequest request) {
        JobOpeningDto created = jobService.createOpening(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<JobOpeningDto>builder()
                .success(true)
                .message("Job opening created successfully")
                .data(created)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission("JOBS_MANAGE")
    public ResponseEntity<ApiResponse<JobOpeningDto>> updateOpening(
            @PathVariable Long id,
            @Valid @RequestBody CreateJobOpeningRequest request) {
        JobOpeningDto updated = jobService.updateOpening(id, request);
        return ResponseEntity.ok(ApiResponse.<JobOpeningDto>builder()
                .success(true)
                .message("Job opening updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @RequirePermission("JOBS_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteOpening(@PathVariable Long id) {
        jobService.deleteOpening(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Job opening deleted successfully")
                .build());
    }

    // =========================================================================
    // CANDIDATE APPLICATIONS
    // =========================================================================

    @GetMapping("/applications")
    @PreAuthorize("hasAnyAuthority('JOBS_VIEW', 'JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission(anyOf = {"JOBS_VIEW", "JOBS_MANAGE"})
    public ResponseEntity<ApiResponse<List<JobApplicationDto>>> getApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        List<JobApplicationDto> list = jobService.getApplications(status, search);
        return ResponseEntity.ok(ApiResponse.<List<JobApplicationDto>>builder()
                .success(true)
                .message("Job applications retrieved successfully")
                .data(list)
                .build());
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasAnyAuthority('JOBS_VIEW', 'JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission(anyOf = {"JOBS_VIEW", "JOBS_MANAGE"})
    public ResponseEntity<ApiResponse<JobApplicationDto>> getApplicationById(@PathVariable Long id) {
        JobApplicationDto dto = jobService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.<JobApplicationDto>builder()
                .success(true)
                .message("Job application retrieved successfully")
                .data(dto)
                .build());
    }

    @PutMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyAuthority('JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_HR')")
    @RequirePermission("JOBS_MANAGE")
    public ResponseEntity<ApiResponse<JobApplicationDto>> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationStatusRequest request) {
        JobApplicationDto updated = jobService.updateApplicationStatus(id, request);
        return ResponseEntity.ok(ApiResponse.<JobApplicationDto>builder()
                .success(true)
                .message("Application status updated to " + updated.getStatus())
                .data(updated)
                .build());
    }

    @DeleteMapping("/applications/{id}")
    @PreAuthorize("hasAnyAuthority('JOBS_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @RequirePermission("JOBS_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id) {
        jobService.deleteApplication(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Job application deleted successfully")
                .build());
    }
}
