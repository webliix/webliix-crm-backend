package com.webliix.finance.controller;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.dto.ExpenseStatisticsResponse;
import com.webliix.finance.enums.ExpenseCategory;
import com.webliix.finance.service.ExpenseService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('EXPENSES_CREATE', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> getExpenses(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExpenseResponse> result = expenseService.getExpenses(category, status, keyword, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ExpenseResponse>>builder()
                .success(true)
                .message("Expenses fetched successfully")
                .data(result)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(@PathVariable Long id) {
        ExpenseResponse response = expenseService.getExpense(id);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense fetched successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXPENSES_EDIT', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @RequestBody ExpenseRequest request
    ) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EXPENSES_DELETE', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Expense deleted successfully")
                .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('EXPENSES_EDIT', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "APPROVED");
        ExpenseResponse response = expenseService.updateExpenseStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .message("Expense status updated")
                .data(response)
                .build());
    }

    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExpenseStatisticsResponse>> getStatistics() {
        ExpenseStatisticsResponse stats = expenseService.getExpenseStatistics();
        return ResponseEntity.ok(ApiResponse.<ExpenseStatisticsResponse>builder()
                .success(true)
                .message("Expense statistics fetched successfully")
                .data(stats)
                .build());
    }
}
