package com.webliix.hr.payroll.repository;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.payroll.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Page<Payroll> findByEmployee(Employee employee, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.grossSalary), 0) FROM Payroll p")
    BigDecimal sumGrossSalary();

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p")
    BigDecimal sumNetSalary();

    @Query("SELECT COALESCE(SUM(p.deductions), 0) FROM Payroll p")
    BigDecimal sumDeductions();
}
