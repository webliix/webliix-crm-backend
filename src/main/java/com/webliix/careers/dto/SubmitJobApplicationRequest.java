package com.webliix.careers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitJobApplicationRequest {
    private Long jobOpeningId;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String applicationType;

    @NotBlank(message = "Applicant name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;
    private String location;
    private String experience;
    private String portfolioUrl;
    private String resumeLink;
    private String message;
    private String payoutMethod;
    private String payoutDetails;
}
