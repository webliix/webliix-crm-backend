package com.webliix.careers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobOpeningRequest {
    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Employment type is required")
    private String type;

    @NotBlank(message = "Location is required")
    private String location;

    private String experience;
    private String description;
    private String requirements;
    private Boolean active;
}
