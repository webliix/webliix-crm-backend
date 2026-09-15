package com.webliix.hr.employee.repository;

import com.webliix.hr.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findTopByEmployeeCodeStartingWithOrderByIdDesc(String prefix);
}
