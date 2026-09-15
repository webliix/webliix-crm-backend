package com.webliix.newsletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriberResponse {

    private Long id;
    private String email;
    private String name;
    private String status;
    private String message;
    private LocalDateTime subscribedAt;
}
