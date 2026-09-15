package com.webliix.finance.service;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseRequest request);
    Page<ExpenseResponse> getAllExpenses(Pageable pageable);
    ExpenseResponse getExpense(Long id);
}
