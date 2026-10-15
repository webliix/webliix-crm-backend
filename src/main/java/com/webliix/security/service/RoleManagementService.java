package com.webliix.security.service;

import com.webliix.security.dto.*;
import com.webliix.security.entity.MenuItem;
import com.webliix.security.entity.UserPermissionOverride;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface RoleManagementService {

    Map<String, List<PermissionDto>> getPermissionsGroupedByModule();

    List<PermissionDto> getAllPermissions();

    List<RoleDto> getAllRoles();

    RoleDto getRole(Long id);

    RoleDto createRole(CreateRoleRequest request);

    RoleDto updateRole(Long id, UpdateRoleRequest request);

    RoleDto updateRolePermissions(Long roleId, List<RolePermissionAssignmentDto> permissions);

    void deleteRole(Long id);

    List<MenuItem> getMenuItems();

    MenuItem saveMenuItem(MenuItem item);

    void deleteMenuItem(Long id);

    void updateUserRoles(Long userId, List<String> roleNames, Authentication auth);

    List<UserPermissionOverride> getUserOverrides(Long userId);

    UserPermissionOverride addOrUpdateOverride(Long userId, PermissionOverrideRequest request);

    void deleteOverride(Long userId, Long permissionId);
}
