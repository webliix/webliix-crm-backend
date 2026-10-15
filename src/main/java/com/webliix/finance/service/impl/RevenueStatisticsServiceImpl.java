package com.webliix.finance.service.impl;

import com.webliix.finance.dto.MonthlyRevenueResponse;
import com.webliix.finance.dto.ProfitResponse;
import com.webliix.finance.dto.RevenueStatisticsResponse;
import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.repository.ExpenseRepository;
import com.webliix.finance.service.RevenueStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueStatisticsServiceImpl implements RevenueStatisticsService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Cacheable(value = "revenueStatistics", key = "'summary'")
    public RevenueStatisticsResponse getRevenueStatistics() {
        long totalInvoices = invoiceRepository.count();
        long paidInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID);
        long overdueInvoices = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);

        BigDecimal totalRevenue = invoiceRepository.sumTotalAmount();
        BigDecimal totalPaid = invoiceRepository.sumPaidAmount();
        BigDecimal totalPending = invoiceRepository.sumPendingAmount();

        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        response.setTotalPaid(totalPaid != null ? totalPaid : BigDecimal.ZERO);
        response.setTotalPending(totalPending != null ? totalPending : BigDecimal.ZERO);
        response.setTotalInvoices(totalInvoices);
        response.setPaidInvoices(paidInvoices);
        response.setOverdueInvoices(overdueInvoices);
        return response;
    }

    @Override
    @Cacheable(value = "revenueStatistics", key = "'monthly'")
    public List<MonthlyRevenueResponse> getMonthlyRevenue() {
        List<Object[]> monthlyData = invoiceRepository.sumTotalAmountGroupByMonth();
        Map<Month, BigDecimal> monthlyTotals = new EnumMap<>(Month.class);

        if (monthlyData != null) {
            for (Object[] row : monthlyData) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    int monthNumber = ((Number) row[0]).intValue();
                    BigDecimal total = (BigDecimal) row[1];
                    if (monthNumber >= 1 && monthNumber <= 12) {
                        monthlyTotals.put(Month.of(monthNumber), total);
                    }
                }
            }
        }

        List<MonthlyRevenueResponse> responses = new ArrayList<>();
        for (Month month : Month.values()) {
            BigDecimal revenue = monthlyTotals.getOrDefault(month, BigDecimal.ZERO);
            MonthlyRevenueResponse response = new MonthlyRevenueResponse();
            response.setMonth(month.name().substring(0, 3));
            response.setRevenue(revenue);
            responses.add(response);
        }
        return responses;
    }

    @Override
    @Cacheable(value = "revenueStatistics", key = "'profit'")
    public ProfitResponse getProfitStatistics() {
        RevenueStatisticsResponse revenueStatistics = getRevenueStatistics();
        BigDecimal totalRevenue = revenueStatistics.getTotalRevenue() != null ? revenueStatistics.getTotalRevenue() : BigDecimal.ZERO;
        BigDecimal totalExpense = expenseRepository.sumTotalAmount();
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        ProfitResponse response = new ProfitResponse();
        response.setTotalRevenue(totalRevenue);
        response.setTotalExpense(totalExpense);
        response.setNetProfit(totalRevenue.subtract(totalExpense));
        return response;
    }
}
