package com.webliix.finance.service.impl;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.entity.Expense;
import com.webliix.finance.repository.ExpenseRepository;
import com.webliix.finance.service.ExpenseService;
import com.webliix.finance.util.ExpenseNumberGenerator;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public ExpenseResponse createExpense(ExpenseRequest request) {
        String prefix = ExpenseNumberGenerator.currentPrefix();
        Expense last = expenseRepository.findTopByExpenseNumberStartingWithOrderByIdDesc(prefix);
        String expenseNumber = last != null
                ? ExpenseNumberGenerator.next(last.getExpenseNumber())
                : prefix + "000001";

        Expense expense = Expense.builder()
                .expenseNumber(expenseNumber)
                .category(request.getCategory())
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .paymentMethod(request.getPaymentMethod())
                .vendor(request.getVendor())
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public ExpenseResponse getExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return toResponse(expense);
    }

    private ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseNumber(expense.getExpenseNumber());
        response.setCategory(expense.getCategory());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setExpenseDate(expense.getExpenseDate());
        response.setPaymentMethod(expense.getPaymentMethod());
        response.setVendor(expense.getVendor());
        response.setCreatedBy(expense.getCreatedBy());
        response.setCreatedAt(expense.getCreatedAt());
        return response;
    }
}
