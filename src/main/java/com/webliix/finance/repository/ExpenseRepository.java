package com.webliix.finance.repository;

import com.webliix.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Expense findTopByExpenseNumberStartingWithOrderByIdDesc(String prefix);
}
