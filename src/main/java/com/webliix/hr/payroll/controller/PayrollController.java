package com.webliix.hr.payroll.controller;

import com.webliix.hr.payroll.dto.PayrollDashboardResponse;
import com.webliix.hr.payroll.dto.PayrollRequest;
import com.webliix.hr.payroll.dto.PayrollResponse;
import com.webliix.hr.payroll.service.PayrollService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PayrollResponse>> generatePayroll(@RequestBody PayrollRequest request) {
        PayrollResponse response = payrollService.generatePayroll(request);
        return ResponseEntity.ok(ApiResponse.<PayrollResponse>builder().success(true).message("Payroll generated successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getPayrolls(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayrollResponse> response = payrollService.getAllPayrolls(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PayrollResponse>>builder().success(true).message("Payroll records fetched").data(response).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayroll(@PathVariable Long id) {
        PayrollResponse response = payrollService.getPayroll(id);
        return ResponseEntity.ok(ApiResponse.<PayrollResponse>builder().success(true).message("Payroll record fetched").data(response).build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getPayrollsForEmployee(@PathVariable Long employeeId,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayrollResponse> response = payrollService.getPayrollsForEmployee(employeeId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PayrollResponse>>builder().success(true).message("Employee payroll records fetched").data(response).build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PayrollDashboardResponse>> getDashboard() {
        PayrollDashboardResponse response = payrollService.getPayrollDashboard();
        return ResponseEntity.ok(ApiResponse.<PayrollDashboardResponse>builder().success(true).message("Payroll dashboard fetched").data(response).build());
    }
}
