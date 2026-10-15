package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMeResponse {
    // Direct fields for backward compatibility
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
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Structured RBAC fields
    private UserProfileResponse user;
    private Set<String> roles;
    @Builder.Default
    private List<UserPermissionItemDto> permissions = new ArrayList<>();
    @Builder.Default
    private List<NavigationItemDto> navigation = new ArrayList<>();
    @Builder.Default
    private List<String> portalAccess = new ArrayList<>();
}
