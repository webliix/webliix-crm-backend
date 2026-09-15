package com.webliix.finance.service.impl;

import com.webliix.finance.dto.MonthlyRevenueResponse;
import com.webliix.finance.dto.ProfitResponse;
import com.webliix.finance.dto.RevenueStatisticsResponse;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.repository.ExpenseRepository;
import com.webliix.finance.service.RevenueStatisticsService;
import lombok.RequiredArgsConstructor;
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
    public RevenueStatisticsResponse getRevenueStatistics() {
        List<Invoice> invoices = invoiceRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        long paidInvoices = 0;
        long overdueInvoices = 0;

        for (Invoice invoice : invoices) {
            if (invoice.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(invoice.getTotalAmount());
            }
            if (invoice.getPaidAmount() != null) {
                totalPaid = totalPaid.add(invoice.getPaidAmount());
            }
            if (invoice.getPendingAmount() != null) {
                totalPending = totalPending.add(invoice.getPendingAmount());
            }
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                paidInvoices++;
            }
            if (invoice.getStatus() == InvoiceStatus.OVERDUE) {
                overdueInvoices++;
            }
        }

        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        response.setTotalRevenue(totalRevenue);
        response.setTotalPaid(totalPaid);
        response.setTotalPending(totalPending);
        response.setTotalInvoices((long) invoices.size());
        response.setPaidInvoices(paidInvoices);
        response.setOverdueInvoices(overdueInvoices);
        return response;
    }

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue() {
        List<Invoice> invoices = invoiceRepository.findAll();
        Map<Month, BigDecimal> monthlyTotals = new EnumMap<>(Month.class);

        for (Invoice invoice : invoices) {
            if (invoice.getTotalAmount() == null) {
                continue;
            }
            if (invoice.getIssueDate() == null) {
                continue;
            }
            Month month = invoice.getIssueDate().getMonth();
            monthlyTotals.put(month, monthlyTotals.getOrDefault(month, BigDecimal.ZERO).add(invoice.getTotalAmount()));
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
    public ProfitResponse getProfitStatistics() {
        RevenueStatisticsResponse revenueStatistics = getRevenueStatistics();
        BigDecimal totalExpense = expenseRepository.findAll().stream()
                .map(expense -> expense.getAmount() == null ? BigDecimal.ZERO : expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ProfitResponse response = new ProfitResponse();
        response.setTotalRevenue(revenueStatistics.getTotalRevenue());
        response.setTotalExpense(totalExpense);
        response.setNetProfit(revenueStatistics.getTotalRevenue().subtract(totalExpense));
        return response;
    }
}
