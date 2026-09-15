package com.webliix.hr.payroll.entity;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.payroll.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payroll_month", nullable = false)
    private String payrollMonth;

    @Column(name = "gross_salary", nullable = false)
    private BigDecimal grossSalary;

    @Column(name = "deductions", nullable = false)
    private BigDecimal deductions;

    @Column(name = "net_salary", nullable = false)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
