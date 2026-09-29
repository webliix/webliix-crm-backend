package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String role;
    private Boolean enabled;
    private Boolean emailVerified;
}
