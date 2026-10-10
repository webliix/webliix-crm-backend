package com.webliix.finance.repository;

import com.webliix.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Expense findTopByExpenseNumberStartingWithOrderByIdDesc(String prefix);

    List<Expense> findByExpenseDateBetween(LocalDate start, LocalDate end);
}
