package com.webliix.security.service;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.security.dto.AuthResponse;
import com.webliix.security.dto.ChangePasswordRequest;
import com.webliix.security.dto.LoginRequest;
import com.webliix.security.dto.RegisterRequest;
import com.webliix.security.dto.UpdateProfileRequest;
import com.webliix.security.dto.UserProfileResponse;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.jwt.JwtService;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role assignedRole = userRepository.count() == 0
                ? roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role SUPER_ADMIN not found"))
                : roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(Set.of(assignedRole))
                .build();

        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(token)
                .user(getCurrentUser(user.getEmail()))
                .build();
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "FAILED", httpRequest);
                    return new ResourceNotFoundException("User not found with email: " + request.getEmail());
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "FAILED", httpRequest);
            throw new IllegalArgumentException("Invalid credentials");
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "SUCCESS", httpRequest);
        return AuthResponse.builder()
                .accessToken(token)
                .user(getCurrentUser(user.getEmail()))
                .build();
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
