package com.webliix.security.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionAssignmentDto {
    @JsonAlias({"permissionCode", "code"})
    private String code;
    private String permissionCode;
    private ScopeType scope;

    public String getCode() {
        if (code != null && !code.isBlank()) return code;
        return permissionCode;
    }

    public String getPermissionCode() {
        if (permissionCode != null && !permissionCode.isBlank()) return permissionCode;
        return code;
    }
}
