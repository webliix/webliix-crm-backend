package com.webliix.hr.leave.service;

import com.webliix.hr.leave.dto.LeaveBalanceRequest;
import com.webliix.hr.leave.dto.LeaveBalanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveBalanceService {
    LeaveBalanceResponse createLeaveBalance(LeaveBalanceRequest request);
    Page<LeaveBalanceResponse> getAllLeaveBalances(Pageable pageable);
    LeaveBalanceResponse getLeaveBalance(Long id);
}
