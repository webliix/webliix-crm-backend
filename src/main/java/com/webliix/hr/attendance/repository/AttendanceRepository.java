package com.webliix.hr.attendance.repository;

import com.webliix.hr.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
}
