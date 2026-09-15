package com.webliix.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSearchRequest {
    private String module;
    private String action;
    private Long userId;
    private String status;
    private String entityType;
    private String entityId;
    private Integer page = 0;
    private Integer size = 25;
}
