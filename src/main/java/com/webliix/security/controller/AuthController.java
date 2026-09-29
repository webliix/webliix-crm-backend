package com.webliix.security.controller;

import com.webliix.security.dto.*;
import com.webliix.security.service.AuthService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, servletRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String msg = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message(msg).data(msg).build());
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<VerifyResetOtpResponse>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        VerifyResetOtpResponse res = authService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.<VerifyResetOtpResponse>builder().success(true).message(res.getMessage()).data(res).build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("Password reset successfully. You may now log in with your new password.").build());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/resend-verification-otp")
    public ResponseEntity<ApiResponse<String>> resendVerificationOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendVerificationOtp(request);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("Verification code sent successfully to your email.").build());
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
            @Valid @RequestBody UpdateProfileRequest request
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
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Void>builder().success(false).message("Unauthorized").build());
        }
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Password changed successfully").build());
    }
}
