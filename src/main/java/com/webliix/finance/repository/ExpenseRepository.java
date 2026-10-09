package com.webliix.finance.repository;

import com.webliix.finance.entity.Expense;
import com.webliix.finance.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Expense findTopByExpenseNumberStartingWithOrderByIdDesc(String prefix);

    @Query("SELECT e FROM Expense e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:status IS NULL OR LOWER(e.status) = LOWER(:status)) AND " +
           "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
           "(:endDate IS NULL OR e.expenseDate <= :endDate) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(e.expenseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(COALESCE(e.title, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(COALESCE(e.vendor, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(COALESCE(e.referenceNumber, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Expense> findWithFilters(
            @Param("category") ExpenseCategory category,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);
}
