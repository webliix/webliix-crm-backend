package com.webliix.security.service;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.security.dto.CachedUserPermissions;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.RolePermission;
import com.webliix.security.entity.ScopeType;
import com.webliix.security.entity.UserPermissionOverride;
import com.webliix.security.model.WebliixPrincipal;
import com.webliix.security.repository.RolePermissionRepository;
import com.webliix.security.repository.UserPermissionOverrideRepository;
import com.webliix.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final EmployeeRepository employeeRepository;
    private final UserPermissionCacheService userPermissionCacheService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        int tokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        CachedUserPermissions cached = userPermissionCacheService.get(user.getId(), tokenVersion);

        if (cached == null) {
            cached = computeUserPermissions(user, tokenVersion);
            userPermissionCacheService.put(user.getId(), tokenVersion, cached);
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String roleName : cached.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        for (String permCode : cached.getPermissions().keySet()) {
            authorities.add(new SimpleGrantedAuthority(permCode));
        }

        if (cached.isSuperAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("SUPER_ADMIN"));
        }

        return WebliixPrincipal.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .enabled(Boolean.TRUE.equals(user.getEnabled()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .tokenVersion(tokenVersion)
                .superAdmin(cached.isSuperAdmin())
                .departmentId(cached.getDepartmentId())
                .employeeId(cached.getEmployeeId())
                .roles(cached.getRoles())
                .permissions(cached.getPermissions())
                .authorities(authorities)
                .build();
    }

    private CachedUserPermissions computeUserPermissions(com.webliix.security.entity.User user, int tokenVersion) {
        Set<String> roleNames = new HashSet<>();
        List<Long> roleIds = new ArrayList<>();
        boolean isSuperAdmin = false;

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (Boolean.FALSE.equals(role.getActive())) {
                    continue;
                }
                String name = role.getName().toUpperCase();
                roleNames.add(name);
                roleIds.add(role.getId());
                if (Boolean.TRUE.equals(role.getIsSuperAdmin()) || "SUPER_ADMIN".equals(name)) {
                    isSuperAdmin = true;
                }
            }
        }

        Map<String, ScopeType> effectivePermissions = new HashMap<>();

        if (!roleIds.isEmpty()) {
            List<RolePermission> rps = rolePermissionRepository.findByRoleIdIn(roleIds);
            for (RolePermission rp : rps) {
                if (rp.getPermission() == null || rp.getPermission().getCode() == null) {
                    continue;
                }
                String code = rp.getPermission().getCode();
                ScopeType currentScope = effectivePermissions.get(code);
                ScopeType newScope = rp.getScope() != null ? rp.getScope() : ScopeType.OWN;

                if (currentScope == null) {
                    effectivePermissions.put(code, newScope);
                } else {
                    // Widest scope wins: ALL (3) > DEPARTMENT (2) > OWN (1)
                    if (newScope.ordinal() > currentScope.ordinal()) {
                        effectivePermissions.put(code, newScope);
                    }
                }
            }
        }

        // Apply user-level permission overrides (GRANT / DENY)
        List<UserPermissionOverride> overrides = userPermissionOverrideRepository.findByUserId(user.getId());
        for (UserPermissionOverride override : overrides) {
            if (override.isExpired()) {
                continue;
            }
            if (override.getPermission() == null || override.getPermission().getCode() == null) {
                continue;
            }
            String code = override.getPermission().getCode();
            if (override.isDeny()) {
                effectivePermissions.remove(code);
            } else if (override.isGrant()) {
                ScopeType overrideScope = override.getScope() != null ? override.getScope() : ScopeType.OWN;
                ScopeType currentScope = effectivePermissions.get(code);
                if (currentScope == null || overrideScope.ordinal() > currentScope.ordinal()) {
                    effectivePermissions.put(code, overrideScope);
                }
            }
        }

        // Resolve linked employee details
        Long departmentId = null;
        Long employeeId = null;
        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            employeeId = emp.getId();
            if (emp.getDepartment() != null) {
                departmentId = emp.getDepartment().getId();
            }
        }

        return CachedUserPermissions.builder()
                .userId(user.getId())
                .tokenVersion(tokenVersion)
                .superAdmin(isSuperAdmin)
                .departmentId(departmentId)
                .employeeId(employeeId)
                .roles(roleNames)
                .permissions(effectivePermissions)
                .build();
    }
}
