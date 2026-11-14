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
                            || norm.equals("ADMIN") || norm.equals("ROLE_ADMIN")
                            || norm.equals("*")) {
                        return true;
                    }
                    if ("CLIENT_PORTAL_ACCESS".equalsIgnoreCase(permissionCode)
                            && (norm.equals("CLIENT") || norm.equals("ROLE_CLIENT") || norm.equals("USER") || norm.equals("ROLE_USER"))) {
                        return true;
                    }
                }
            }
            if ("CLIENT_PORTAL_ACCESS".equalsIgnoreCase(permissionCode) && webliixPrincipal.hasPermission("CLIENT_PORTAL_ACCESS")) {
                return true;
            }
            return webliixPrincipal.hasPermission(permissionCode);
        }
        // Fallback for standard authorities
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String aUpper = a.getAuthority().toUpperCase();
                    if (aUpper.equalsIgnoreCase(permissionCode) || aUpper.equalsIgnoreCase("ROLE_" + permissionCode)) {
                        return true;
                    }
                    if (aUpper.equals("ROLE_SUPER_ADMIN") || aUpper.equals("SUPER_ADMIN")
                            || aUpper.equals("ROLE_ADMIN") || aUpper.equals("ADMIN")
                            || aUpper.equals("*")) {
                        return true;
                    }
                    if ("CLIENT_PORTAL_ACCESS".equalsIgnoreCase(permissionCode)
                            && (aUpper.equals("ROLE_CLIENT") || aUpper.equals("CLIENT") || aUpper.equals("ROLE_USER") || aUpper.equals("USER"))) {
                        return true;
                    }
                    return false;
                });
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
