package com.webliix.security;

import com.webliix.security.evaluator.PermissionEvaluator;
import com.webliix.security.model.WebliixPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RbacMatrixTest {

    // Seeded permissions map for non-superadmin testing (equivalent to V62 seed matrix)
    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "ADMIN", Set.of(
                    "EXPENSES_VIEW", "EXPENSES_CREATE", "EXPENSES_EDIT", "EXPENSES_APPROVE", "EXPENSES_PAY", "EXPENSES_DELETE", "EXPENSES_EXPORT",
                    "INVOICES_VIEW", "INVOICES_CREATE", "INVOICES_EDIT", "INVOICES_DELETE", "INVOICES_SEND", "INVOICES_EXPORT",
                    "PAYMENTS_VIEW", "PAYMENTS_RECORD", "PAYMENT_SUBMISSIONS_VIEW", "PAYMENT_SUBMISSIONS_REVIEW",
                    "QUOTATIONS_VIEW", "QUOTATIONS_CREATE", "QUOTATIONS_EDIT", "QUOTATIONS_DELETE", "QUOTATIONS_CONVERT",
                    "BILLING_VIEW", "BILLING_MANAGE",
                    "REPORTS_FINANCE_VIEW", "REPORTS_SALES_VIEW", "REPORTS_HR_VIEW", "REPORTS_PROJECTS_VIEW", "REPORTS_EXPORT",
                    "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_DELETE", "LEADS_CONVERT", "LEADS_ASSIGN",
                    "CUSTOMERS_VIEW", "CUSTOMERS_CREATE", "CUSTOMERS_EDIT", "CUSTOMERS_DELETE",
                    "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT", "PROJECTS_DELETE", "PROJECTS_BILLING_VIEW",
                    "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT", "TICKETS_ASSIGN", "TICKETS_DELETE",
                    "WORKLOGS_SUBMIT", "WORKLOGS_VIEW", "WORKLOGS_REVIEW",
                    "EMPLOYEES_VIEW", "EMPLOYEES_MANAGE", "EMPLOYEES_SALARY_VIEW", "DEPARTMENTS_MANAGE", "DESIGNATIONS_MANAGE",
                    "ATTENDANCE_VIEW", "ATTENDANCE_MANAGE",
                    "LEAVE_APPLY", "LEAVE_VIEW", "LEAVE_APPROVE", "LEAVE_BALANCE_MANAGE",
                    "PAYROLL_VIEW", "PAYROLL_GENERATE", "PAYROLL_PAY", "PAYSLIP_VIEW_OWN",
                    "BLOGS_MANAGE", "NEWSLETTER_MANAGE", "REVIEWS_MANAGE", "OFFERS_MANAGE",
                    "USERS_MANAGE", "ROLES_MANAGE", "SETTINGS_VIEW", "SETTINGS_MANAGE", "AUDIT_VIEW",
                    "AUTOMATION_MANAGE", "STORAGE_UPLOAD", "STORAGE_MANAGE", "API_TOKENS_MANAGE", "MONITORING_VIEW",
                    "NOTIFICATIONS_BROADCAST", "EMAIL_TEMPLATES_MANAGE", "DASHBOARD_VIEW"
            ),
            "MANAGER", Set.of(
                    "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT", "DASHBOARD_VIEW",
                    "EXPENSES_VIEW", "EXPENSES_APPROVE", "INVOICES_VIEW", "QUOTATIONS_VIEW",
                    "REPORTS_SALES_VIEW", "REPORTS_PROJECTS_VIEW", "CUSTOMERS_VIEW", "LEADS_VIEW",
                    "EMPLOYEES_VIEW", "ATTENDANCE_VIEW", "LEAVE_VIEW", "LEAVE_APPROVE",
                    "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT", "TICKETS_ASSIGN",
                    "WORKLOGS_VIEW", "WORKLOGS_REVIEW",
                    "EXPENSES_CREATE", "LEAVE_APPLY", "PAYSLIP_VIEW_OWN"
            ),
            "HR", Set.of(
                    "EMPLOYEES_VIEW", "EMPLOYEES_MANAGE", "EMPLOYEES_SALARY_VIEW", "DEPARTMENTS_MANAGE", "DESIGNATIONS_MANAGE",
                    "ATTENDANCE_VIEW", "ATTENDANCE_MANAGE", "LEAVE_VIEW", "LEAVE_APPROVE", "LEAVE_BALANCE_MANAGE",
                    "PAYROLL_VIEW", "PAYROLL_GENERATE", "PAYROLL_PAY", "REPORTS_HR_VIEW", "DASHBOARD_VIEW",
                    "EXPENSES_CREATE", "LEAVE_APPLY", "PAYSLIP_VIEW_OWN"
            ),
            "EMPLOYEE", Set.of(
                    "EMPLOYEE_PORTAL_ACCESS",
                    "EXPENSES_CREATE", "EXPENSES_VIEW",
                    "PAYMENT_SUBMISSIONS_SUBMIT", "PAYMENT_SUBMISSIONS_VIEW",
                    "WORKLOGS_SUBMIT", "WORKLOGS_VIEW",
                    "ATTENDANCE_VIEW", "LEAVE_APPLY", "LEAVE_VIEW", "PAYSLIP_VIEW_OWN",
                    "PROJECTS_VIEW", "TICKETS_VIEW", "TICKETS_CREATE"
            ),
            "CLIENT", Set.of("CLIENT_PORTAL_ACCESS"),
            "USER", Set.of("CLIENT_PORTAL_ACCESS")
    );

    private final PermissionEvaluator evaluator = new PermissionEvaluator();

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "Role {0} should have {1} = {2}")
    @CsvFileSource(resources = "/rbac-matrix.csv", numLinesToSkip = 1)
    @DisplayName("Evaluate RBAC permissions matrix against PermissionEvaluator")
    public void testRbacMatrix(String role, String permission, boolean expectedAllowed) {
        boolean isSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(role);

        Map<String, com.webliix.security.entity.ScopeType> permsMap = new HashMap<>();
        Set<String> rolePerms = ROLE_PERMISSIONS.getOrDefault(role.toUpperCase(), Set.of());
        for (String p : rolePerms) {
            permsMap.put(p, com.webliix.security.entity.ScopeType.ALL);
        }

        WebliixPrincipal principal = WebliixPrincipal.builder()
                .userId(101L)
                .username("test-" + role.toLowerCase() + "@webliix.in")
                .roles(Set.of(role.toUpperCase()))
                .permissions(permsMap)
                .superAdmin(isSuperAdmin)
                .departmentId(1L)
                .employeeId(202L)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "n/a", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        boolean actualAllowed = evaluator.has(permission);
        assertEquals(expectedAllowed, actualAllowed,
                String.format("Role %s check for permission %s expected %b but got %b",
                        role, permission, expectedAllowed, actualAllowed));
    }
}
