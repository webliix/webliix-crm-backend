package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String jobTitle;
    private String department;
    private String bio;
    private String timezone;
    private String language;
    private Boolean twoFactorEnabled;
}
