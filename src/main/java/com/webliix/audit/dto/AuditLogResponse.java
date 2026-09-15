package com.webliix.audit.dto;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private AuditAction action;
    private AuditModule module;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String status;
    private LocalDateTime createdAt;
}
