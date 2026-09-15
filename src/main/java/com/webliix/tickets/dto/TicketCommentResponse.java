package com.webliix.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentResponse {

    private Long id;
    private Long ticketId;
    private String comment;
    private String commentedBy;
    private LocalDateTime createdAt;
}
