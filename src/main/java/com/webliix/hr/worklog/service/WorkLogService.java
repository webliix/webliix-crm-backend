package com.webliix.hr.worklog.service;

import com.webliix.hr.worklog.dto.WorkLogRequest;
import com.webliix.hr.worklog.dto.WorkLogResponse;
import com.webliix.hr.worklog.dto.WorkLogReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

public interface WorkLogService {
    WorkLogResponse submitWorkLog(WorkLogRequest request, Authentication auth);
    WorkLogResponse updateWorkLog(Long id, WorkLogRequest request, Authentication auth);
    WorkLogResponse getWorkLog(Long id, Authentication auth);
    Page<WorkLogResponse> getMyWorkLogs(Pageable pageable, Authentication auth);
    List<WorkLogResponse> getMyWorkLogsByDateRange(LocalDate from, LocalDate to, Authentication auth);
    Page<WorkLogResponse> getAllWorkLogs(Pageable pageable);
    Page<WorkLogResponse> getWorkLogsByEmployee(Long employeeId, Pageable pageable);
    WorkLogResponse reviewWorkLog(Long id, WorkLogReviewRequest request, Authentication auth);
    void deleteWorkLog(Long id, Authentication auth);
}
