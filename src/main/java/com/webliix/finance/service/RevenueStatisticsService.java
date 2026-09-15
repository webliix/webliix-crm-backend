package com.webliix.finance.service;

import com.webliix.finance.dto.MonthlyRevenueResponse;
import com.webliix.finance.dto.ProfitResponse;
import com.webliix.finance.dto.RevenueStatisticsResponse;

import java.util.List;

public interface RevenueStatisticsService {
    RevenueStatisticsResponse getRevenueStatistics();
    List<MonthlyRevenueResponse> getMonthlyRevenue();
    ProfitResponse getProfitStatistics();
}
