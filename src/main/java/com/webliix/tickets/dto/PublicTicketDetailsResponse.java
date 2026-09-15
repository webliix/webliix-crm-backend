package com.webliix.tickets.dto;

import com.webliix.tickets.enums.TicketCategory;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTicketDetailsResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private String customerName;
    private String customerEmail;
    private String createdBy;
    private TicketPriority priority;
    private TicketStatus status;
    private TicketCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketCommentResponse> comments;
}
