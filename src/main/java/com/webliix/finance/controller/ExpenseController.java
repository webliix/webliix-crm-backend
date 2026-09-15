package com.webliix.finance.controller;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.service.ExpenseService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder().success(true).message("Expense created successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpenses(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExpenseResponse> result = expenseService.getAllExpenses(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ExpenseResponse>>builder().success(true).message("Expenses fetched").data(result).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpense(id);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder().success(true).message("Expense fetched").data(response).build());
    }
}
