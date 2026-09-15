package com.webliix.hr.attendance.service.impl;

import com.webliix.hr.attendance.dto.AttendanceRequest;
import com.webliix.hr.attendance.dto.AttendanceResponse;
import com.webliix.hr.attendance.entity.Attendance;
import com.webliix.hr.attendance.repository.AttendanceRepository;
import com.webliix.hr.attendance.service.AttendanceService;
import com.webliix.hr.attendance.enums.AttendanceStatus;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceResponse recordAttendance(AttendanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus() != null ? request.getStatus() : AttendanceStatus.PRESENT);

        if (request.getCheckInTime() != null && request.getCheckOutTime() != null) {
            if (request.getCheckOutTime().isBefore(request.getCheckInTime())) {
                throw new IllegalArgumentException("Check-out time cannot be before check-in time");
            }
            java.time.Duration duration = java.time.Duration.between(request.getCheckInTime(), request.getCheckOutTime());
            attendance.setWorkingHours(duration.toMinutes() / 60.0);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Override
    public Page<AttendanceResponse> getAllAttendance(Pageable pageable) {
        return attendanceRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<AttendanceResponse> getEmployeeAttendance(Long employeeId, Pageable pageable) {
        return attendanceRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse);
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(attendance.getId());
        if (attendance.getEmployee() != null) {
            response.setEmployeeId(attendance.getEmployee().getId());
        }
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setWorkingHours(attendance.getWorkingHours());
        response.setStatus(attendance.getStatus());
        return response;
    }
}
