package com.webliix.security.evaluator;

import com.webliix.security.entity.ScopeType;
import com.webliix.security.model.WebliixPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("perm")
@Slf4j
public class PermissionEvaluator {

    public boolean has(String permissionCode) {
        return has(null, permissionCode);
    }

    public boolean has(Object root, String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        // 1. Super Admin and Admin roles always bypass all permission checks
        boolean isPrivileged = auth.getAuthorities().stream().anyMatch(a -> {
            String authName = a.getAuthority().toUpperCase();
            return authName.equals("ROLE_SUPER_ADMIN") || authName.equals("SUPER_ADMIN")
                    || authName.equals("ROLE_ADMIN") || authName.equals("ADMIN");
        });
        if (isPrivileged) {
            return true;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof WebliixPrincipal webliixPrincipal) {
            if (webliixPrincipal.isSuperAdmin()) {
                return true;
            }
            if (webliixPrincipal.getRoles() != null) {
                for (String r : webliixPrincipal.getRoles()) {
                    String norm = r.toUpperCase();
                    if (norm.equals("SUPER_ADMIN") || norm.equals("ROLE_SUPER_ADMIN")
                            || norm.equals("ADMIN") || norm.equals("ROLE_ADMIN")) {
                        return true;
                    }
                }
            }
            return webliixPrincipal.hasPermission(permissionCode);
        }
        // Fallback for standard authorities
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(permissionCode)
                        || a.getAuthority().equalsIgnoreCase("ROLE_" + permissionCode)
                        || a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equalsIgnoreCase("SUPER_ADMIN")
                        || a.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
                        || a.getAuthority().equalsIgnoreCase("ADMIN"));
    }

    public boolean hasAny(String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }
        return Arrays.stream(permissionCodes).anyMatch(this::has);
    }

    public ScopeType scope(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        boolean isPrivileged = auth.getAuthorities().stream().anyMatch(a -> {
            String authName = a.getAuthority().toUpperCase();
            return authName.equals("ROLE_SUPER_ADMIN") || authName.equals("SUPER_ADMIN")
                    || authName.equals("ROLE_ADMIN") || authName.equals("ADMIN");
        });
        if (isPrivileged) {
            return ScopeType.ALL;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof WebliixPrincipal webliixPrincipal) {
            if (webliixPrincipal.isSuperAdmin()) {
                return ScopeType.ALL;
            }
            if (webliixPrincipal.getRoles() != null) {
                for (String r : webliixPrincipal.getRoles()) {
                    String norm = r.toUpperCase();
                    if (norm.equals("SUPER_ADMIN") || norm.equals("ROLE_SUPER_ADMIN")
                            || norm.equals("ADMIN") || norm.equals("ROLE_ADMIN")) {
                        return ScopeType.ALL;
                    }
                }
            }
            return webliixPrincipal.getPermissionScope(permissionCode);
        }
        return null;
    }
}
