package com.webliix.hr.attendance.controller;

import com.webliix.hr.attendance.dto.AttendanceRequest;
import com.webliix.hr.attendance.dto.AttendanceResponse;
import com.webliix.hr.attendance.service.AttendanceService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/attendance")
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordAttendance(@RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.recordAttendance(request);
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder().success(true).message("Attendance recorded successfully").data(response).build());
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendance(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceResponse> response = attendanceService.getAllAttendance(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<AttendanceResponse>>builder().success(true).message("Attendance records fetched").data(response).build());
    }

    @GetMapping("/employees/{employeeId}/attendance")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getEmployeeAttendance(@PathVariable Long employeeId,
                                                                                       @RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceResponse> response = attendanceService.getEmployeeAttendance(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<AttendanceResponse>>builder().success(true).message("Employee attendance fetched").data(response).build());
    }
}
