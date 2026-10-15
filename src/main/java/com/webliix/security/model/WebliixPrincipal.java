package com.webliix.security.model;

import com.webliix.security.entity.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Builder
@AllArgsConstructor
public class WebliixPrincipal implements UserDetails {

    private final Long userId;
    private final String username; // email
    private final String password;
    private final boolean enabled;
    private final boolean emailVerified;
    private final Integer tokenVersion;
    private final boolean superAdmin;
    private final Long departmentId;
    private final Long employeeId;
    private final Set<String> roles;
    private final Map<String, ScopeType> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasPermission(String permissionCode) {
        if (superAdmin) {
            return true;
        }
        return permissions != null && permissions.containsKey(permissionCode);
    }

    public ScopeType getPermissionScope(String permissionCode) {
        if (superAdmin) {
            return ScopeType.ALL;
        }
        return permissions != null ? permissions.get(permissionCode) : null;
    }
}
