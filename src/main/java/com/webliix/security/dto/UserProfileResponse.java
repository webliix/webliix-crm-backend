package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private String department;
    private String bio;
    private String timezone;
    private String language;
    private Boolean twoFactorEnabled;
    private Boolean enabled;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
