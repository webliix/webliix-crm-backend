package com.webliix.tickets.dto;

import java.time.LocalDate;

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
public class UpdateTicketRequest {

    private String title;
    private String description;
    private Long customerId;
    private Long projectId;
    private String createdBy;
    private Long assignedToId;
    private TicketPriority priority;
    private TicketStatus status;
    private TicketCategory category;
    private LocalDate dueDate;
    private Integer slaHours;
    private Integer responseTime;
    private Integer resolutionTime;
}
