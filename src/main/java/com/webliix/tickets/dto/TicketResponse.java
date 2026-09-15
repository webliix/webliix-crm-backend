package com.webliix.tickets.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.webliix.tickets.enums.TicketCategory;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private Long customerId;
    private String customerName;
    private Long projectId;
    private String projectName;
    private String createdBy;
    private Long assignedToId;
    private String assignedToName;
    private TicketPriority priority;
    private TicketStatus status;
    private TicketCategory category;
    private LocalDate dueDate;
    private LocalDateTime closedAt;
    private Integer slaHours;
    private Integer responseTime;
    private Integer resolutionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
