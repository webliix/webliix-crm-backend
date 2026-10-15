package com.webliix.security.dto;

import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionOverrideRequest {
    private String permissionCode;
    private String effect; // GRANT or DENY
    private ScopeType scope; // OWN, DEPARTMENT, ALL
    private LocalDateTime expiresAt;
}
