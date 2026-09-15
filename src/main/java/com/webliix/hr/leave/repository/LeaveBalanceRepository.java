package com.webliix.hr.leave.repository;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.leave.entity.LeaveBalance;
import com.webliix.hr.leave.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeAndLeaveType(Employee employee, LeaveType leaveType);
}
