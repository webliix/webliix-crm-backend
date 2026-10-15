package com.webliix.security.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;

    private String password;

    private String portal; // e.g. "EMPLOYEE", "CLIENT"
}
