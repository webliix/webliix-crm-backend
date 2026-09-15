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
public class TicketAttachmentResponse {

    private Long id;
    private Long ticketId;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadedAt;
}
