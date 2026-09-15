package com.webliix.hr.attendance.dto;

import com.webliix.hr.attendance.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double workingHours;
    private AttendanceStatus status;
}
