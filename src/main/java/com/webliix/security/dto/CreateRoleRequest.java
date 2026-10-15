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
public class CreateRoleRequest {
    private String name;
    private String displayName;
    private String description;
    @Builder.Default
    private List<RolePermissionAssignmentDto> permissions = new ArrayList<>();
}
