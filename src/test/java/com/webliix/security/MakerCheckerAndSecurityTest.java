package com.webliix.security;

import com.webliix.finance.entity.Expense;
import com.webliix.finance.repository.ExpenseRepository;
import com.webliix.finance.service.impl.ExpenseServiceImpl;
import com.webliix.security.evaluator.PermissionEvaluator;
import com.webliix.security.scoping.DataScopeService;
import com.webliix.audit.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MakerCheckerAndSecurityTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private DataScopeService dataScopeService;

    @Mock
    private PermissionEvaluator permissionEvaluator;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("Maker-Checker: User can NEVER approve their own expense, even if they have EXPENSES_APPROVE")
    public void testUserCannotApproveOwnExpense() {
        Long userId = 42L;
        Long expenseId = 100L;

        Expense ownExpense = Expense.builder()
                .id(expenseId)
                .createdByUserId(userId)
                .status("PENDING")
                .build();

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(ownExpense));
        when(dataScopeService.getCurrentUserId()).thenReturn(userId);
        when(permissionEvaluator.has("EXPENSES_APPROVE")).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            expenseService.updateExpenseStatus(expenseId, "APPROVED");
        });

        assertTrue(ex.getMessage().contains("Maker-checker violation"),
                "Expected Maker-checker violation message, got: " + ex.getMessage());
    }

    @Test
    @DisplayName("Maker-Checker: User can NEVER reject their own expense")
    public void testUserCannotRejectOwnExpense() {
        Long userId = 42L;
        Long expenseId = 101L;

        Expense ownExpense = Expense.builder()
                .id(expenseId)
                .createdByUserId(userId)
                .status("PENDING")
                .build();

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(ownExpense));
        when(dataScopeService.getCurrentUserId()).thenReturn(userId);
        when(permissionEvaluator.has("EXPENSES_APPROVE")).thenReturn(true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            expenseService.updateExpenseStatus(expenseId, "REJECTED");
        });

        assertTrue(ex.getMessage().contains("Maker-checker violation"),
                "Expected Maker-checker violation message, got: " + ex.getMessage());
    }
}
