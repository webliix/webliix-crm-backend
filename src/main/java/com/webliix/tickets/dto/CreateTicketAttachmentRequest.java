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
public class CreateTicketAttachmentRequest {

    @NotBlank
    private String fileName;

    @NotBlank
    private String filePath;

    private String fileType;
}
