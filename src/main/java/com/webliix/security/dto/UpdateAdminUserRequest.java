package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminUserRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String jobTitle;
    private String department;
    private String bio;
    private String role; // Backward compatible single role
    private List<String> roles; // Multi-role support
    private Boolean enabled;
    private Boolean emailVerified;
}
