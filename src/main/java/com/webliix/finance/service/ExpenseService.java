package com.webliix.finance.service;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.dto.ExpenseStatisticsResponse;
import com.webliix.finance.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseRequest request);
    Page<ExpenseResponse> getAllExpenses(Pageable pageable);
    Page<ExpenseResponse> getExpenses(ExpenseCategory category, String status, String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable);
    ExpenseResponse getExpense(Long id);
    ExpenseResponse updateExpense(Long id, ExpenseRequest request);
    void deleteExpense(Long id);
    ExpenseResponse updateExpenseStatus(Long id, String status);
    ExpenseStatisticsResponse getExpenseStatistics();
}
