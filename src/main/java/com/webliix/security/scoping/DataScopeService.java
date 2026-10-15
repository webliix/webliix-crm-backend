package com.webliix.security.scoping;

import com.webliix.security.entity.ScopeType;
import com.webliix.security.model.WebliixPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataScopeService {

    public DataScope resolve(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated request");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof WebliixPrincipal p) {
            if (p.isSuperAdmin()) {
                return DataScope.builder()
                        .scope(ScopeType.ALL)
                        .userId(p.getUserId())
                        .employeeId(p.getEmployeeId())
                        .departmentId(p.getDepartmentId())
                        .superAdmin(true)
                        .build();
            }

            ScopeType scope = p.getPermissionScope(permissionCode);
            if (scope == null) {
                log.warn("User {} lacks permission {}", p.getUsername(), permissionCode);
                throw new AccessDeniedException("Missing required permission: " + permissionCode);
            }

            return DataScope.builder()
                    .scope(scope)
                    .userId(p.getUserId())
                    .employeeId(p.getEmployeeId())
                    .departmentId(p.getDepartmentId())
                    .superAdmin(false)
                    .build();
        }

        // Fallback for system / tests with super admin
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));

        return DataScope.builder()
                .scope(isSuperAdmin ? ScopeType.ALL : ScopeType.OWN)
                .superAdmin(isSuperAdmin)
                .build();
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof WebliixPrincipal p) {
            return p.getUserId();
        }
        return null;
    }

    public Long getCurrentEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof WebliixPrincipal p) {
            return p.getEmployeeId();
        }
        return null;
    }
}
