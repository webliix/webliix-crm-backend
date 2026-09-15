package com.webliix.hr.payroll.service;

import com.webliix.hr.payroll.dto.PayrollDashboardResponse;
import com.webliix.hr.payroll.dto.PayrollRequest;
import com.webliix.hr.payroll.dto.PayrollResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {
    PayrollResponse generatePayroll(PayrollRequest request);
    Page<PayrollResponse> getAllPayrolls(Pageable pageable);
    PayrollResponse getPayroll(Long id);
    Page<PayrollResponse> getPayrollsForEmployee(Long employeeId, Pageable pageable);
    PayrollDashboardResponse getPayrollDashboard();
}
