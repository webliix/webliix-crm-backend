package com.webliix.projects.entity;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.projects.enums.ProjectPriority;
import com.webliix.projects.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_code", unique = true)
    private String projectCode;

    @Column(name = "project_name")
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal budget;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    private ProjectPriority priority;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    private Boolean billable;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

