package com.webliix.security.dto;

import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedUserPermissions implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer tokenVersion;
    private boolean superAdmin;
    private Long departmentId;
    private Long employeeId;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Map<String, ScopeType> permissions = new HashMap<>();
}
