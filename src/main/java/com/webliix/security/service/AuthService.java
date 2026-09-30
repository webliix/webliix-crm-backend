package com.webliix.security.service;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.notifications.service.EmailService;
import com.webliix.security.dto.*;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.jwt.JwtService;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuditService auditService;
    private final OtpService otpService;
    private final RateLimiterService rateLimiterService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role assignedRole = userRepository.count() == 0
                ? roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role SUPER_ADMIN not found"))
                : roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));

        boolean isSuperAdmin = "SUPER_ADMIN".equals(assignedRole.getName());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .emailVerified(isSuperAdmin) // Auto-verify initial bootstrap admin
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(Set.of(assignedRole))
                .build();

        userRepository.save(user);

        // Generate and dispatch Email Verification OTP if not super admin
        if (!isSuperAdmin) {
            try {
                String otp = otpService.generateAndStoreOtp(OtpService.PREFIX_EMAIL_VERIFICATION, user.getEmail());
                emailService.sendVerificationOtpEmail(user.getEmail(), user.getFirstName(), otp, 5);
            } catch (Exception e) {
                log.error("Failed to send email verification OTP during registration: {}", e.getMessage());
            }
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(token)
                .user(getCurrentUser(user.getEmail()))
                .build();
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String emailKey = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        rateLimiterService.checkRateLimit("login:" + emailKey, 5, 900, "login");

        var user = userRepository.findByEmail(emailKey)
                .orElseThrow(() -> {
                    auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "FAILED", httpRequest);
                    return new ResourceNotFoundException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "FAILED", httpRequest);
            throw new IllegalArgumentException("Invalid email or password");
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "SUCCESS", httpRequest);
        return AuthResponse.builder()
                .accessToken(token)
                .user(getCurrentUser(user.getEmail()))
                .build();
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String token = jwtService.generateToken(userDetails);
            return AuthResponse.builder()
                    .accessToken(token)
                    .user(getCurrentUser(user.getEmail()))
                    .build();
        }

        boolean isValid = otpService.verifyOtp(OtpService.PREFIX_EMAIL_VERIFICATION, email, request.getOtp());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(token)
                .user(getCurrentUser(user.getEmail()))
                .build();
    }

    public void resendVerificationOtp(ResendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email is already verified.");
        }

        String otp = otpService.generateAndStoreOtp(OtpService.PREFIX_EMAIL_VERIFICATION, email);
        emailService.sendVerificationOtpEmail(user.getEmail(), user.getFirstName(), otp, 5);
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        rateLimiterService.checkRateLimit("forgot-password:" + email, 3, 3600, "forgot password");

        userRepository.findByEmail(email).ifPresent(user -> {
            try {
                String otp = otpService.generateAndStoreOtp(OtpService.PREFIX_PASSWORD_RESET, email);
                emailService.sendForgotPasswordOtpEmail(user.getEmail(), user.getFirstName(), otp, 5);
            } catch (Exception e) {
                log.error("Failed to process forgot password request: {}", e.getMessage());
            }
        });

        // Always return generic response to prevent user enumeration
        return "If an account with that email exists, a 6-digit verification code has been sent.";
    }

    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        rateLimiterService.checkRateLimit("verify-reset-otp:" + email, 10, 300, "OTP verification");

        boolean isValid = otpService.verifyOtp(OtpService.PREFIX_PASSWORD_RESET, email, request.getOtp());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        String resetToken = otpService.createResetToken(email);
        return VerifyResetOtpResponse.builder()
                .email(email)
                .resetToken(resetToken)
                .message("OTP verified successfully. You may now reset your password.")
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        boolean isValidToken = otpService.verifyResetToken(email, request.getResetToken());
        if (!isValidToken) {
            throw new IllegalArgumentException("Invalid or expired password reset session. Please request a new code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Single-use: Clear reset token
        otpService.clearResetToken(email);

        // Send confirmation email
        try {
            emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send password reset success email: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<String> permissions = resolvePermissionsForRoles(roleNames);

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle() != null ? user.getJobTitle() : "Lead Architecture Director")
                .department(user.getDepartment() != null ? user.getDepartment() : "Engineering & Product")
                .bio(user.getBio() != null ? user.getBio() : "Architecting high-speed enterprise CRM solutions, multi-tenant microservices, and AI workflow tools for Webliix SaaS platform.")
                .timezone(user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata")
                .language(user.getLanguage() != null ? user.getLanguage() : "en-US")
                .twoFactorEnabled(user.getTwoFactorEnabled() != null ? user.getTwoFactorEnabled() : false)
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified() != null ? user.getEmailVerified() : false)
                .roles(roleNames)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Set<String> resolvePermissionsForRoles(Set<String> roles) {
        if (roles.contains("SUPER_ADMIN")) {
            return Set.of(
                "*",
                "DASHBOARD_VIEW",
                "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_DELETE", "LEADS_CONVERT",
                "CLIENTS_VIEW", "CLIENTS_CREATE", "CLIENTS_EDIT", "CLIENTS_DELETE",
                "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT", "PROJECTS_DELETE",
                "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT", "TICKETS_DELETE",
                "INVOICES_VIEW", "INVOICES_CREATE", "INVOICES_EDIT", "INVOICES_DELETE",
                "REPORTS_VIEW", "SETTINGS_VIEW", "HR_VIEW", "EXPENSES_VIEW", "AUDIT_VIEW"
            );
        }

        Set<String> perms = new HashSet<>();
        perms.add("DASHBOARD_VIEW");

        if (roles.contains("ADMIN")) {
            perms.addAll(Set.of(
                "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_DELETE", "LEADS_CONVERT",
                "CLIENTS_VIEW", "CLIENTS_CREATE", "CLIENTS_EDIT", "CLIENTS_DELETE",
                "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT", "PROJECTS_DELETE",
                "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT", "TICKETS_DELETE",
                "INVOICES_VIEW", "INVOICES_CREATE", "INVOICES_EDIT", "INVOICES_DELETE",
                "REPORTS_VIEW", "HR_VIEW", "EXPENSES_VIEW"
            ));
        }

        if (roles.contains("MANAGER")) {
            perms.addAll(Set.of(
                "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_CONVERT",
                "CLIENTS_VIEW", "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT",
                "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT",
                "INVOICES_VIEW", "REPORTS_VIEW"
            ));
        }

        if (roles.contains("EMPLOYEE")) {
            perms.addAll(Set.of(
                "LEADS_VIEW", "CLIENTS_VIEW", "PROJECTS_VIEW", "TICKETS_VIEW"
            ));
        }

        if (roles.contains("HR")) {
            perms.addAll(Set.of(
                "HR_VIEW", "HR_MANAGE", "EMPLOYEES_VIEW", "EMPLOYEES_MANAGE", "ATTENDANCE_VIEW", "PAYROLL_VIEW", "LEAVE_VIEW"
            ));
        }

        if (roles.contains("USER") || roles.contains("CLIENT") || roles.contains("ROLE_CLIENT")) {
            perms.addAll(Set.of(
                "PROJECTS_VIEW", "INVOICES_VIEW", "TICKETS_VIEW", "TICKETS_CREATE",
                "CUSTOMER_PROJECTS_VIEW", "CUSTOMER_NOTIFICATIONS_VIEW", "PUBLIC_UPDATES_VIEW"
            ));
        }

        return perms;
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle().trim());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone().trim());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage().trim());
        }
        if (request.getTwoFactorEnabled() != null) {
            user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return getCurrentUser(email);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
