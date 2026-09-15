package com.webliix.hr.leave.service;

import com.webliix.hr.leave.dto.LeaveRequestDto;
import com.webliix.hr.leave.dto.LeaveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveService {
    LeaveResponse applyLeave(LeaveRequestDto request);
    Page<LeaveResponse> getAllLeaves(Pageable pageable);
    LeaveResponse getLeave(Long id);
    LeaveResponse approveLeave(Long id);
    LeaveResponse rejectLeave(Long id);
}
