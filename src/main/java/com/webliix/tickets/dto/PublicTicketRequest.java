package com.webliix.tickets.dto;

import com.webliix.tickets.enums.TicketCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTicketRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Subject/Title is required")
    private String title;

    @NotBlank(message = "Query message is required")
    private String description;

    private TicketCategory category;
}
