package com.webliix.security.controller;

import com.webliix.security.dto.AuthResponse;
import com.webliix.security.dto.ChangePasswordRequest;
import com.webliix.security.dto.LoginRequest;
import com.webliix.security.dto.RegisterRequest;
import com.webliix.security.dto.UpdateProfileRequest;
import com.webliix.security.dto.UserProfileResponse;
import com.webliix.security.service.AuthService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, servletRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.<UserProfileResponse>builder().success(false).message("Unauthorized").build());
        }
        UserProfileResponse profile = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder().success(true).message("Profile retrieved successfully").data(profile).build());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.<UserProfileResponse>builder().success(false).message("Unauthorized").build());
        }
        UserProfileResponse profile = authService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder().success(true).message("Profile updated successfully").data(profile).build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Void>builder().success(false).message("Unauthorized").build());
        }
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Password changed successfully").build());
    }
}
