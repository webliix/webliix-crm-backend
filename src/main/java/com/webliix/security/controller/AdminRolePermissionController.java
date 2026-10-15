package com.webliix.security.controller;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.dto.*;
import com.webliix.security.entity.MenuItem;
import com.webliix.security.service.RoleManagementService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminRolePermissionController {

    private final RoleManagementService roleManagementService;

    @GetMapping("/permissions")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<Map<String, List<PermissionDto>>>> getPermissions() {
        var grouped = roleManagementService.getPermissionsGroupedByModule();
        return ResponseEntity.ok(ApiResponse.<Map<String, List<PermissionDto>>>builder()
                .success(true)
                .message("Permission catalog fetched")
                .data(grouped)
                .build());
    }

    @GetMapping("/roles")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        var roles = roleManagementService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.<List<RoleDto>>builder()
                .success(true)
                .message("Roles fetched")
                .data(roles)
                .build());
    }

    @GetMapping("/roles/{id}")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<RoleDto>> getRole(@PathVariable Long id) {
        var role = roleManagementService.getRole(id);
        return ResponseEntity.ok(ApiResponse.<RoleDto>builder()
                .success(true)
                .message("Role fetched")
                .data(role)
                .build());
    }

    @PostMapping("/roles")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody CreateRoleRequest request) {
        var role = roleManagementService.createRole(request);
        return ResponseEntity.ok(ApiResponse.<RoleDto>builder()
                .success(true)
                .message("Role created successfully")
                .data(role)
                .build());
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        var role = roleManagementService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.<RoleDto>builder()
                .success(true)
                .message("Role updated successfully")
                .data(role)
                .build());
    }

    @PutMapping("/roles/{id}/permissions")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<RoleDto>> updateRolePermissions(
            @PathVariable Long id,
            @RequestBody List<RolePermissionAssignmentDto> permissions) {
        var role = roleManagementService.updateRolePermissions(id, permissions);
        return ResponseEntity.ok(ApiResponse.<RoleDto>builder()
                .success(true)
                .message("Role permissions updated successfully")
                .data(role)
                .build());
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleManagementService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Role deleted successfully")
                .build());
    }

    @GetMapping("/menu-items")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<List<MenuItem>>> getMenuItems() {
        var items = roleManagementService.getMenuItems();
        return ResponseEntity.ok(ApiResponse.<List<MenuItem>>builder()
                .success(true)
                .message("Menu items fetched")
                .data(items)
                .build());
    }

    @PostMapping("/menu-items")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<MenuItem>> saveMenuItem(@RequestBody MenuItem item) {
        var saved = roleManagementService.saveMenuItem(item);
        return ResponseEntity.ok(ApiResponse.<MenuItem>builder()
                .success(true)
                .message("Menu item saved")
                .data(saved)
                .build());
    }

    @DeleteMapping("/menu-items/{id}")
    @RequirePermission("ROLES_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        roleManagementService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Menu item deleted")
                .build());
    }
}
