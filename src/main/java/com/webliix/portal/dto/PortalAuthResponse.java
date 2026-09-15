package com.webliix.portal.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PortalAuthResponse {
    private Long id;
    private String email;
    private String token;
    private LocalDateTime lastLogin;
}
