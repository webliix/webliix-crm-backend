package com.webliix.finance.service.impl;

import com.webliix.finance.dto.ExpenseRequest;
import com.webliix.finance.dto.ExpenseResponse;
import com.webliix.finance.dto.ExpenseStatisticsResponse;
import com.webliix.finance.entity.Expense;
import com.webliix.finance.enums.ExpenseCategory;
import com.webliix.finance.repository.ExpenseRepository;
import com.webliix.finance.service.ExpenseService;
import com.webliix.finance.util.ExpenseNumberGenerator;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        String prefix = ExpenseNumberGenerator.currentPrefix();
        Expense last = expenseRepository.findTopByExpenseNumberStartingWithOrderByIdDesc(prefix);
        String expenseNumber = last != null
                ? ExpenseNumberGenerator.next(last.getExpenseNumber())
                : prefix + "000001";

        String status = request.getStatus();
        if (status == null || status.isBlank()) {
            status = "APPROVED";
        }

        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = request.getDescription();
        }

        Expense expense = Expense.builder()
                .expenseNumber(expenseNumber)
                .title(title)
                .category(request.getCategory())
                .description(request.getDescription())
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .vendor(request.getVendor())
                .status(status.toUpperCase())
                .notes(request.getNotes())
                .referenceNumber(request.getReferenceNumber())
                .receiptUrl(request.getReceiptUrl())
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<ExpenseResponse> getExpenses(ExpenseCategory category, String status, String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Expense> spec = buildExpenseSpecification(category, status, keyword, startDate, endDate);
        return expenseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private Specification<Expense> buildExpenseSpecification(ExpenseCategory category, String status, String keyword, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status.trim())) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate expNumLike = cb.like(cb.lower(root.get("expenseNumber")), likePattern);
                Predicate titleLike = cb.like(cb.lower(cb.coalesce(root.get("title"), "")), likePattern);
                Predicate descLike = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), likePattern);
                Predicate vendorLike = cb.like(cb.lower(cb.coalesce(root.get("vendor"), "")), likePattern);
                Predicate refLike = cb.like(cb.lower(cb.coalesce(root.get("referenceNumber"), "")), likePattern);

                predicates.add(cb.or(expNumLike, titleLike, descLike, vendorLike, refLike));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public ExpenseResponse getExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return toResponse(expense);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            expense.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getVendor() != null) {
            expense.setVendor(request.getVendor());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            expense.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getNotes() != null) {
            expense.setNotes(request.getNotes());
        }
        if (request.getReferenceNumber() != null) {
            expense.setReferenceNumber(request.getReferenceNumber());
        }
        if (request.getReceiptUrl() != null) {
            expense.setReceiptUrl(request.getReceiptUrl());
        }
        expense.setUpdatedAt(LocalDateTime.now());

        Expense updated = expenseRepository.save(expense);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpenseStatus(Long id, String status) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        expense.setStatus(status.toUpperCase());
        expense.setUpdatedAt(LocalDateTime.now());
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    public ExpenseStatisticsResponse getExpenseStatistics() {
        List<Expense> all = expenseRepository.findAll();
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal mtd = BigDecimal.ZERO;
        BigDecimal approved = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        Map<String, BigDecimal> breakdown = new HashMap<>();

        for (Expense e : all) {
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            total = total.add(amt);

            if (e.getExpenseDate() != null && !e.getExpenseDate().isBefore(firstDayOfMonth) && !e.getExpenseDate().isAfter(now)) {
                mtd = mtd.add(amt);
            }

            String st = e.getStatus() != null ? e.getStatus().toUpperCase() : "APPROVED";
            if ("APPROVED".equals(st) || "PAID".equals(st)) {
                approved = approved.add(amt);
            } else if ("PENDING".equals(st)) {
                pending = pending.add(amt);
            }

            String cat = e.getCategory() != null ? e.getCategory().name() : "OTHER";
            breakdown.put(cat, breakdown.getOrDefault(cat, BigDecimal.ZERO).add(amt));
        }

        ExpenseStatisticsResponse stats = new ExpenseStatisticsResponse();
        stats.setTotalExpenses(total);
        stats.setMonthToDateExpenses(mtd);
        stats.setApprovedExpenses(approved);
        stats.setPendingExpenses(pending);
        stats.setTotalCount(all.size());
        stats.setCategoryBreakdown(breakdown);
        return stats;
    }

    private ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setExpenseNumber(expense.getExpenseNumber());
        response.setTitle(expense.getTitle() != null ? expense.getTitle() : expense.getDescription());
        response.setCategory(expense.getCategory());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setExpenseDate(expense.getExpenseDate());
        response.setPaymentMethod(expense.getPaymentMethod());
        response.setVendor(expense.getVendor());
        response.setStatus(expense.getStatus() != null ? expense.getStatus() : "APPROVED");
        response.setNotes(expense.getNotes());
        response.setReferenceNumber(expense.getReferenceNumber());
        response.setReceiptUrl(expense.getReceiptUrl());
        response.setCreatedBy(expense.getCreatedBy());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
