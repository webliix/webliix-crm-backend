package com.webliix.finance.controller;

import com.webliix.finance.dto.MonthlyRevenueResponse;
import com.webliix.finance.dto.ProfitResponse;
import com.webliix.finance.dto.RevenueStatisticsResponse;
import com.webliix.finance.service.RevenueStatisticsService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final RevenueStatisticsService revenueStatisticsService;

    @GetMapping("/revenue")
    @RequirePermission("REPORTS_FINANCE_VIEW")
    public ResponseEntity<ApiResponse<RevenueStatisticsResponse>> getRevenueStatistics() {
        RevenueStatisticsResponse response = revenueStatisticsService.getRevenueStatistics();
        return ResponseEntity.ok(ApiResponse.<RevenueStatisticsResponse>builder().success(true).message("Revenue statistics fetched").data(response).build());
    }

    @GetMapping("/sales")
    @RequirePermission("REPORTS_SALES_VIEW")
    public ResponseEntity<ApiResponse<RevenueStatisticsResponse>> getSalesStatistics() {
        RevenueStatisticsResponse response = revenueStatisticsService.getRevenueStatistics();
        return ResponseEntity.ok(ApiResponse.<RevenueStatisticsResponse>builder().success(true).message("Sales revenue statistics fetched").data(response).build());
    }

    @GetMapping("/revenue/monthly")
    @RequirePermission("REPORTS_FINANCE_VIEW")
    public ResponseEntity<ApiResponse<List<MonthlyRevenueResponse>>> getMonthlyRevenue() {
        List<MonthlyRevenueResponse> response = revenueStatisticsService.getMonthlyRevenue();
        return ResponseEntity.ok(ApiResponse.<List<MonthlyRevenueResponse>>builder().success(true).message("Monthly revenue fetched").data(response).build());
    }

    @GetMapping("/profit")
    @RequirePermission("REPORTS_FINANCE_VIEW")
    public ResponseEntity<ApiResponse<ProfitResponse>> getProfitStatistics() {
        ProfitResponse response = revenueStatisticsService.getProfitStatistics();
        return ResponseEntity.ok(ApiResponse.<ProfitResponse>builder().success(true).message("Profit statistics fetched").data(response).build());
    }
}
