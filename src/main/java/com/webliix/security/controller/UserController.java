package com.webliix.security.controller;

import com.webliix.security.dto.CreateUserRequest;
import com.webliix.security.dto.UpdateAdminUserRequest;
import com.webliix.security.dto.UserProfileResponse;
import com.webliix.security.service.UserService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USERS_MANAGE', 'SUPER_ADMIN', 'ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('USERS_MANAGE', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User details retrieved")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USERS_MANAGE', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User account created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USERS_MANAGE', 'SUPER_ADMIN', 'ADMIN')")
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
    @PreAuthorize("hasAnyAuthority('USERS_DELETE', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User account deleted successfully")
                .build());
    }
}
