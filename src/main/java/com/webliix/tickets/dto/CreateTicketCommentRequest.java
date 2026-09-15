package com.webliix.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketCommentRequest {

    @NotBlank
    private String comment;

    @NotBlank
    private String commentedBy;
}
