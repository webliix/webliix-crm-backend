package com.webliix.hr.worklog.controller;

import com.webliix.hr.worklog.dto.WorkLogRequest;
import com.webliix.hr.worklog.dto.WorkLogResponse;
import com.webliix.hr.worklog.dto.WorkLogReviewRequest;
import com.webliix.hr.worklog.service.WorkLogService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    // === EMPLOYEE SELF-SERVICE ROUTES ===

    @PostMapping("/employee/me/work-logs")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE', 'SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<WorkLogResponse>> submitWorkLog(
            @RequestBody WorkLogRequest request,
            Authentication auth) {
        WorkLogResponse response = workLogService.submitWorkLog(request, auth);
        return ResponseEntity.ok(ApiResponse.<WorkLogResponse>builder()
                .success(true).message("Work log submitted successfully").data(response).build());
    }

    @PutMapping("/employee/me/work-logs/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE', 'SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<WorkLogResponse>> updateWorkLog(
            @PathVariable Long id,
            @RequestBody WorkLogRequest request,
            Authentication auth) {
        WorkLogResponse response = workLogService.updateWorkLog(id, request, auth);
        return ResponseEntity.ok(ApiResponse.<WorkLogResponse>builder()
                .success(true).message("Work log updated successfully").data(response).build());
    }

    @GetMapping("/employee/me/work-logs")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE', 'SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<WorkLogResponse>>> getMyWorkLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkLogResponse> response = workLogService.getMyWorkLogs(pageable, auth);
        return ResponseEntity.ok(ApiResponse.<Page<WorkLogResponse>>builder()
                .success(true).message("Work logs fetched successfully").data(response).build());
    }

    @GetMapping("/employee/me/work-logs/range")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE', 'SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<WorkLogResponse>>> getMyWorkLogsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        List<WorkLogResponse> response = workLogService.getMyWorkLogsByDateRange(from, to, auth);
        return ResponseEntity.ok(ApiResponse.<List<WorkLogResponse>>builder()
                .success(true).message("Work logs in range fetched successfully").data(response).build());
    }

    // === ADMIN/MANAGER MANAGEMENT ROUTES ===

    @GetMapping("/work-logs")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'HR', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Page<WorkLogResponse>>> getAllWorkLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkLogResponse> response = workLogService.getAllWorkLogs(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<WorkLogResponse>>builder()
                .success(true).message("All work logs fetched").data(response).build());
    }

    @GetMapping("/work-logs/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'HR', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Page<WorkLogResponse>>> getWorkLogsByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkLogResponse> response = workLogService.getWorkLogsByEmployee(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<WorkLogResponse>>builder()
                .success(true).message("Employee work logs fetched").data(response).build());
    }

    @GetMapping("/work-logs/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkLogResponse>> getWorkLog(
            @PathVariable Long id,
            Authentication auth) {
        WorkLogResponse response = workLogService.getWorkLog(id, auth);
        return ResponseEntity.ok(ApiResponse.<WorkLogResponse>builder()
                .success(true).message("Work log fetched").data(response).build());
    }

    @PatchMapping("/work-logs/{id}/review")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'HR', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<WorkLogResponse>> reviewWorkLog(
            @PathVariable Long id,
            @RequestBody WorkLogReviewRequest request,
            Authentication auth) {
        WorkLogResponse response = workLogService.reviewWorkLog(id, request, auth);
        return ResponseEntity.ok(ApiResponse.<WorkLogResponse>builder()
                .success(true).message("Work log review saved").data(response).build());
    }

    @DeleteMapping("/work-logs/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteWorkLog(
            @PathVariable Long id,
            Authentication auth) {
        workLogService.deleteWorkLog(id, auth);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Work log deleted successfully").build());
    }
}
