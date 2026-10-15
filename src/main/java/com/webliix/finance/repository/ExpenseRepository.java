package com.webliix.finance.repository;

import com.webliix.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Expense findTopByExpenseNumberStartingWithOrderByIdDesc(String prefix);

    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE UPPER(COALESCE(e.status, 'APPROVED')) IN ('APPROVED', 'PAID')")
    BigDecimal sumApprovedAmount();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE UPPER(e.status) = 'PENDING'")
    BigDecimal sumPendingAmount();

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e GROUP BY e.category")
    List<Object[]> sumAmountByCategory();
}
