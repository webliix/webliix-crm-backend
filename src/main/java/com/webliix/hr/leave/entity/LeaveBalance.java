package com.webliix.hr.leave.entity;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.leave.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "allocation_days", nullable = false)
    private BigDecimal allocationDays;

    @Column(name = "used_days", nullable = false)
    private BigDecimal usedDays;

    @Column(name = "remaining_days", nullable = false)
    private BigDecimal remainingDays;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
