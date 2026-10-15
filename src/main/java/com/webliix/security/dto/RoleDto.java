package com.webliix.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private Boolean isSystem;
    private Boolean isSuperAdmin;
    private Boolean active;
    private Long defaultDepartmentId;
    private long userCount;
    @Builder.Default
    private List<RolePermissionAssignmentDto> permissions = new ArrayList<>();
}
