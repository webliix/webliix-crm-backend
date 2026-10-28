package com.webliix.careers.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationDto {
    private Long id;
    private Long jobOpeningId;
    private String jobTitle;
    private String applicationType;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String experience;
    private String portfolioUrl;
    private String resumeLink;
    private String message;
    private String payoutMethod;
    private String payoutDetails;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
