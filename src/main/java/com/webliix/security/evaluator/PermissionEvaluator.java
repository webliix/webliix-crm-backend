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
        Object principal = auth.getPrincipal();
        if (principal instanceof WebliixPrincipal webliixPrincipal) {
            if (webliixPrincipal.isSuperAdmin()) {
                return true;
            }
            return webliixPrincipal.hasPermission(permissionCode);
        }
        // Fallback for standard authorities
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permissionCode)
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("SUPER_ADMIN"));
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
        Object principal = auth.getPrincipal();
        if (principal instanceof WebliixPrincipal webliixPrincipal) {
            if (webliixPrincipal.isSuperAdmin()) {
                return ScopeType.ALL;
            }
            return webliixPrincipal.getPermissionScope(permissionCode);
        }
        return null;
    }
}
