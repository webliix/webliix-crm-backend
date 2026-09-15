package com.webliix.hr.payroll.repository;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.payroll.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Page<Payroll> findByEmployee(Employee employee, Pageable pageable);
}
