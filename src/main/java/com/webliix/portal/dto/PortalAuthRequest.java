package com.webliix.portal.dto;

import lombok.Data;

@Data
public class PortalAuthRequest {
    private String email;
    private String password;
}
