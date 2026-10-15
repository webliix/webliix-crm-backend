package com.webliix.security.scoping;

import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScope {
    private ScopeType scope;
    private Long userId;
    private Long employeeId;
    private Long departmentId;
    private boolean superAdmin;

    public boolean isAll() {
        return superAdmin || scope == ScopeType.ALL;
    }

    public boolean isDepartment() {
        return !isAll() && scope == ScopeType.DEPARTMENT;
    }

    public boolean isOwn() {
        return !isAll() && scope == ScopeType.OWN;
    }
}
