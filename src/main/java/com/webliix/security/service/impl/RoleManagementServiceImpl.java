package com.webliix.security.service.impl;

import com.webliix.security.dto.*;
import com.webliix.security.entity.*;
import com.webliix.security.model.WebliixPrincipal;
import com.webliix.security.repository.*;
import com.webliix.security.service.RoleManagementService;
import com.webliix.security.service.UserPermissionCacheService;
import com.webliix.shared.exceptions.BusinessException;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final UserPermissionCacheService userPermissionCacheService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<PermissionDto>> getPermissionsGroupedByModule() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(this::toPermissionDto)
                .collect(Collectors.groupingBy(PermissionDto::getModule, LinkedHashMap::new, Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(this::toPermissionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toRoleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return toRoleDto(role);
    }

    @Override
    @Transactional
    public RoleDto createRole(CreateRoleRequest request) {
        String roleName = request.getName().trim().toUpperCase();
        if (roleRepository.existsByName(roleName)) {
            throw new BusinessException("Role with name " + roleName + " already exists.");
        }

        Role role = Role.builder()
                .name(roleName)
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : roleName)
                .description(request.getDescription())
                .isSystem(false)
                .isSuperAdmin(false)
                .active(true)
                .build();

        Role savedRole = roleRepository.save(role);

        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            saveRolePermissions(savedRole, request.getPermissions());
        }

        return toRoleDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (request.getDisplayName() != null) {
            role.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            if (Boolean.TRUE.equals(role.getIsSuperAdmin()) && !request.getActive()) {
                throw new BusinessException("Cannot deactivate the SUPER_ADMIN role.");
            }
            role.setActive(request.getActive());
        }

        Role updated = roleRepository.save(role);

        // Evict cache for all users holding this role
        evictUsersForRole(role.getId());

        return toRoleDto(updated);
    }

    @Override
    @Transactional
    public RoleDto updateRolePermissions(Long roleId, List<RolePermissionAssignmentDto> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (Boolean.TRUE.equals(role.getIsSuperAdmin())) {
            throw new BusinessException("SUPER_ADMIN role permissions cannot be customized; SUPER_ADMIN possesses all permissions by system design.");
        }

        // Remove existing role_permissions
        rolePermissionRepository.deleteByRoleId(roleId);
        roleRepository.flush();

        if (permissions != null && !permissions.isEmpty()) {
            saveRolePermissions(role, permissions);
        }

        // Evict cache and bump token version for all users with this role
        evictUsersForRole(roleId);

        return toRoleDto(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException("System roles cannot be deleted.");
        }

        long assignedUsers = userRepository.countByRolesContaining(role);
        if (assignedUsers > 0) {
            throw new BusinessException("Role cannot be deleted while assigned to " + assignedUsers + " user(s).");
        }

        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItems() {
        return menuItemRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    @Transactional
    public MenuItem saveMenuItem(MenuItem item) {
        return menuItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<String> roleNames, Authentication auth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean callerIsSuperAdmin = isCallerSuperAdmin(auth);

        Set<String> newRoleNames = roleNames.stream().map(String::toUpperCase).collect(Collectors.toSet());
        Set<String> currentRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        // Guard 1: Only SUPER_ADMIN can assign or remove SUPER_ADMIN
        if ((newRoleNames.contains("SUPER_ADMIN") || currentRoleNames.contains("SUPER_ADMIN")) && !callerIsSuperAdmin) {
            throw new AccessDeniedException("Only SUPER_ADMIN can assign or revoke the SUPER_ADMIN role.");
        }

        // Guard 2: Nobody can remove the last active SUPER_ADMIN
        if (currentRoleNames.contains("SUPER_ADMIN") && !newRoleNames.contains("SUPER_ADMIN")) {
            long superAdminCount = userRepository.countActiveSuperAdmins();
            if (superAdminCount <= 1) {
                throw new BusinessException("Cannot remove the last active SUPER_ADMIN user.");
            }
        }

        // Guard 3: Non-superadmin ADMIN cannot grant permissions they do not hold themselves
        if (!callerIsSuperAdmin && auth.getPrincipal() instanceof WebliixPrincipal callerPrincipal) {
            for (String targetRoleName : newRoleNames) {
                if (!currentRoleNames.contains(targetRoleName)) {
                    Role targetRole = roleRepository.findByName(targetRoleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + targetRoleName));
                    List<RolePermission> targetPerms = rolePermissionRepository.findByRoleId(targetRole.getId());
                    for (RolePermission rp : targetPerms) {
                        String code = rp.getPermission().getCode();
                        if (!callerPrincipal.hasPermission(code)) {
                            throw new AccessDeniedException("You cannot assign role '" + targetRoleName + "' containing permission '" + code + "' which you do not possess.");
                        }
                    }
                }
            }
        }

        Set<Role> updatedRoles = new HashSet<>();
        for (String rName : newRoleNames) {
            Role r = roleRepository.findByName(rName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + rName));
            updatedRoles.add(r);
        }

        user.setRoles(updatedRoles);
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Evict permission cache
        userPermissionCacheService.evict(userId);
        log.info("Updated roles for user {} to {}. Incremented token_version to {}", userId, newRoleNames, user.getTokenVersion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermissionOverride> getUserOverrides(Long userId) {
        return userPermissionOverrideRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserPermissionOverride addOrUpdateOverride(Long userId, PermissionOverrideRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Permission permission = permissionRepository.findByCode(request.getPermissionCode())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + request.getPermissionCode()));

        UserPermissionOverride override = userPermissionOverrideRepository
                .findByUserIdAndPermissionId(userId, permission.getId())
                .orElse(UserPermissionOverride.builder()
                        .user(user)
                        .permission(permission)
                        .build());

        override.setEffect(request.getEffect().toUpperCase());
        override.setScope(request.getScope() != null ? request.getScope() : ScopeType.OWN);
        override.setExpiresAt(request.getExpiresAt());
        override.setUpdatedAt(LocalDateTime.now());

        UserPermissionOverride saved = userPermissionOverrideRepository.save(override);

        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        userRepository.save(user);

        userPermissionCacheService.evict(userId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteOverride(Long userId, Long permissionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userPermissionOverrideRepository.deleteByUserIdAndPermissionId(userId, permissionId);

        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        userRepository.save(user);

        userPermissionCacheService.evict(userId);
    }

    private void saveRolePermissions(Role role, List<RolePermissionAssignmentDto> permissions) {
        for (RolePermissionAssignmentDto assignment : permissions) {
            Permission perm = permissionRepository.findByCode(assignment.getCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + assignment.getCode()));
            ScopeType scope = assignment.getScope() != null ? assignment.getScope() : ScopeType.OWN;
            RolePermission rp = new RolePermission(role, perm, scope);
            rolePermissionRepository.save(rp);
        }
    }

    private void evictUsersForRole(Long roleId) {
        List<User> affectedUsers = userRepository.findByRoleId(roleId);
        for (User u : affectedUsers) {
            int currentVersion = u.getTokenVersion() != null ? u.getTokenVersion() : 0;
            u.setTokenVersion(currentVersion + 1);
            userRepository.save(u);
            userPermissionCacheService.evict(u.getId());
        }
        log.info("Evicted permissions and bumped token_version for {} users with roleId {}", affectedUsers.size(), roleId);
    }

    private boolean isCallerSuperAdmin(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof WebliixPrincipal principal) {
            return principal.isSuperAdmin();
        }
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
    }

    private RoleDto toRoleDto(Role role) {
        List<RolePermission> rps = rolePermissionRepository.findByRoleId(role.getId());
        List<RolePermissionAssignmentDto> perms = rps.stream()
                .map(rp -> RolePermissionAssignmentDto.builder()
                        .code(rp.getPermission().getCode())
                        .scope(rp.getScope())
                        .build())
                .collect(Collectors.toList());

        long count = userRepository.countByRolesContaining(role);

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName() != null ? role.getDisplayName() : role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .isSuperAdmin(role.getIsSuperAdmin())
                .active(role.getActive())
                .defaultDepartmentId(role.getDefaultDepartmentId())
                .userCount(count)
                .permissions(perms)
                .build();
    }

    private PermissionDto toPermissionDto(Permission p) {
        return PermissionDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .module(p.getModule())
                .action(p.getAction())
                .description(p.getDescription())
                .build();
    }
}
