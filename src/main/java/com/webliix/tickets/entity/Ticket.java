package com.webliix.tickets.entity;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.projects.entity.Project;
import com.webliix.tickets.enums.TicketCategory;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "created_by")
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "resolution_time")
    private Integer resolutionTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
