package com.webliix.security.controller;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.dto.*;
import com.webliix.security.entity.UserPermissionOverride;
import com.webliix.security.service.RoleManagementService;
import com.webliix.security.service.UserService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleManagementService roleManagementService;

    @GetMapping
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserProfileResponse> response = userService.getAllUsers(search, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserProfileResponse>>builder()
                .success(true)
                .message("Users fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User details retrieved")
                .data(response)
                .build());
    }

    @PostMapping
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User account created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminUserRequest request
    ) {
        UserProfileResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User account updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("USERS_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User account deleted successfully")
                .build());
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @PathVariable Long id,
            @RequestBody UserRolesUpdateRequest request,
            Authentication auth) {
        roleManagementService.updateUserRoles(id, request.getRoles(), auth);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User roles updated successfully")
                .build());
    }

    @GetMapping("/{id}/permission-overrides")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<List<UserPermissionOverride>>> getUserOverrides(@PathVariable Long id) {
        var overrides = roleManagementService.getUserOverrides(id);
        return ResponseEntity.ok(ApiResponse.<List<UserPermissionOverride>>builder()
                .success(true)
                .message("User overrides fetched")
                .data(overrides)
                .build());
    }

    @PutMapping("/{id}/permission-overrides")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<UserPermissionOverride>> addOrUpdateOverride(
            @PathVariable Long id,
            @RequestBody PermissionOverrideRequest request) {
        var override = roleManagementService.addOrUpdateOverride(id, request);
        return ResponseEntity.ok(ApiResponse.<UserPermissionOverride>builder()
                .success(true)
                .message("Permission override saved")
                .data(override)
                .build());
    }

    @DeleteMapping("/{id}/permission-overrides/{permissionId}")
    @RequirePermission("USERS_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteOverride(
            @PathVariable Long id,
            @PathVariable Long permissionId) {
        roleManagementService.deleteOverride(id, permissionId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Permission override removed")
                .build());
    }
}
