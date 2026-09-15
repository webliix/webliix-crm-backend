package com.webliix.hr.attendance.service;

import com.webliix.hr.attendance.dto.AttendanceRequest;
import com.webliix.hr.attendance.dto.AttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceService {
    AttendanceResponse recordAttendance(AttendanceRequest request);
    Page<AttendanceResponse> getAllAttendance(Pageable pageable);
    Page<AttendanceResponse> getEmployeeAttendance(Long employeeId, Pageable pageable);
}
