package com.webliix.hr.employee.entity;

import com.webliix.hr.department.entity.Department;
import com.webliix.hr.designation.entity.Designation;
import com.webliix.hr.employee.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", unique = true, nullable = false)
    private String employeeCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private Boolean active;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String state;
    private String country;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
