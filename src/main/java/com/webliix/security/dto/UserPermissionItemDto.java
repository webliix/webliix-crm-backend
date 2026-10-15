package com.webliix.security.dto;

import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionItemDto {
    private String code;
    private ScopeType scope;
}
