package com.webliix.hr.worklog.repository;

import com.webliix.hr.worklog.entity.WorkLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    Page<WorkLog> findByEmployeeId(Long employeeId, Pageable pageable);
    List<WorkLog> findByEmployeeIdAndLogDateBetweenOrderByLogDateDesc(Long employeeId, LocalDate from, LocalDate to);
    Page<WorkLog> findByStatus(String status, Pageable pageable);
    Page<WorkLog> findByProjectId(Long projectId, Pageable pageable);
}
