package com.webliix.careers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobApplicationStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // NEW, REVIEWING, SHORTLISTED, REJECTED, HIRED

    private String notes;
}
