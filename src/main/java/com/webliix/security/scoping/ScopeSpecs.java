package com.webliix.security.scoping;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.finance.entity.Expense;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.entity.Quotation;
import com.webliix.hr.paymentsubmission.entity.PaymentSubmission;
import com.webliix.hr.worklog.entity.WorkLog;
import org.springframework.data.jpa.domain.Specification;

public final class ScopeSpecs {

    private ScopeSpecs() {}

    public static Specification<Expense> expenseScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        if (scope.isDepartment() && scope.getDepartmentId() != null) {
            return (root, query, cb) -> cb.or(
                    cb.equal(root.get("employee").get("department").get("id"), scope.getDepartmentId()),
                    cb.equal(root.get("createdByUserId"), scope.getUserId())
            );
        }
        // OWN scope
        return (root, query, cb) -> cb.equal(root.get("createdByUserId"), scope.getUserId());
    }

    public static Specification<Lead> leadScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("assignedTo"), scope.getUserId());
    }

    public static Specification<Customer> customerScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("ownerId"), scope.getUserId());
    }

    public static Specification<Invoice> invoiceScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("createdByUserId"), scope.getUserId());
    }

    public static Specification<Quotation> quotationScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("createdByUserId"), scope.getUserId());
    }

    public static Specification<WorkLog> workLogScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        if (scope.isDepartment() && scope.getDepartmentId() != null) {
            return (root, query, cb) -> cb.equal(root.get("employee").get("department").get("id"), scope.getDepartmentId());
        }
        return (root, query, cb) -> cb.equal(root.get("employee").get("id"), scope.getEmployeeId());
    }

    public static Specification<PaymentSubmission> paymentSubmissionScope(DataScope scope) {
        if (scope.isAll()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("employee").get("id"), scope.getEmployeeId());
    }
}
