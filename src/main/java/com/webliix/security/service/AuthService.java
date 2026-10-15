package com.webliix.security.service;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.notifications.service.EmailService;
import com.webliix.security.dto.*;
import com.webliix.security.entity.MenuItem;
import com.webliix.security.entity.Permission;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.ScopeType;
import com.webliix.security.entity.User;
import com.webliix.security.jwt.JwtService;
import com.webliix.security.model.WebliixPrincipal;
import com.webliix.security.repository.MenuItemRepository;
import com.webliix.security.repository.PermissionRepository;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.BusinessException;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuditService auditService;
    private final OtpService otpService;
    private final RateLimiterService rateLimiterService;
    private final EmailService emailService;
    private final UserPermissionCacheService userPermissionCacheService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        long userCount = userRepository.count();
        // Allow ONLY the initial bootstrap super admin account; afterwards disable public registration
        if (userCount > 0) {
            throw new BusinessException("Public registration is disabled. Internal accounts must be created by an administrator.");
        }

        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("Role SUPER_ADMIN not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .emailVerified(true) // Initial bootstrap admin is pre-verified
                .tokenVersion(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(Set.of(superAdminRole))
                .build();

        userRepository.save(user);
        log.info("Bootstrapped initial SUPER_ADMIN account: {}", user.getEmail());

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

        if (Boolean.FALSE.equals(user.getEnabled())) {
            auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "DISABLED_ACCOUNT", httpRequest);
            throw new BusinessException("Account is disabled. Please contact your administrator.");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "UNVERIFIED_EMAIL", httpRequest);
            throw new BusinessException("Email not verified. Please verify your email before logging in.");
        }

        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(r -> r.getName().toUpperCase()).collect(Collectors.toSet())
                : Collections.emptySet();

        boolean isSuperAdmin = roleNames.contains("SUPER_ADMIN") || user.getRoles().stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsSuperAdmin()));
        boolean hasCrmAdminRole = isSuperAdmin
                || roleNames.contains("ADMIN")
                || roleNames.contains("MANAGER")
                || roleNames.contains("HR")
                || roleNames.contains("ACCOUNTS")
                || roleNames.contains("SALES")
                || roleNames.contains("SUPPORT")
                || roleNames.contains("MARKETING");

        // Enforce employee portal login: users with ONLY employee status cannot log into administrative portals
        boolean isPureEmployee = roleNames.contains("EMPLOYEE") && !hasCrmAdminRole;
        if (isPureEmployee) {
            String portalHeader = httpRequest != null ? httpRequest.getHeader("X-Portal") : null;
            String portalReq = request.getPortal();
            boolean isFromEmployeePortal = "EMPLOYEE".equalsIgnoreCase(portalHeader) || "EMPLOYEE".equalsIgnoreCase(portalReq);
            if (!isFromEmployeePortal) {
                auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "EMPLOYEE_PORTAL_REQUIRED", httpRequest);
                throw new AccessDeniedException("Access denied: Employee accounts must log in through the Employee Portal.");
            }
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
        rateLimiterService.checkRateLimit("forgot-password:" + email, 5, 3600, "forgot password");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            var customerOpt = customerRepository.findByEmail(email);
            if (customerOpt.isPresent()) {
                var customer = customerOpt.get();
                Role clientRole = roleRepository.findByName("USER")
                        .or(() -> roleRepository.findByName("CLIENT"))
                        .orElseThrow(() -> new ResourceNotFoundException("Client role not configured"));
                user = User.builder()
                        .firstName(customer.getContactPerson() != null && !customer.getContactPerson().isBlank() ? customer.getContactPerson() : customer.getCompanyName())
                        .lastName("")
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .phone(customer.getPhone())
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .roles(Set.of(clientRole))
                        .build();
                user = userRepository.save(user);
                log.info("Auto-provisioned User account for existing Webliix customer: {}", email);
            }
        }

        if (user == null) {
            var employeeOpt = employeeRepository.findByEmail(email);
            if (employeeOpt.isPresent()) {
                var emp = employeeOpt.get();
                Role employeeRole = roleRepository.findByName("EMPLOYEE")
                        .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));
                user = User.builder()
                        .firstName(emp.getFirstName())
                        .lastName(emp.getLastName())
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .phone(emp.getPhone())
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .roles(Set.of(employeeRole))
                        .build();
                user = userRepository.save(user);
                emp.setUser(user);
                employeeRepository.save(emp);
                log.info("Auto-provisioned User account for existing Webliix employee: {}", email);
            }
        }

        if (user == null) {
            throw new ResourceNotFoundException("User not found with this email address");
        }

        try {
            String otp = otpService.generateAndStoreOtp(OtpService.PREFIX_PASSWORD_RESET, email);
            emailService.sendForgotPasswordOtpEmail(user.getEmail(), user.getFirstName(), otp, 5);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Failed to process forgot password request for {}: {}", email, e.getMessage());
            throw new BusinessException("Failed to dispatch verification code: " + e.getMessage());
        }

        return "A 6-digit verification code has been sent to " + email;
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
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        userPermissionCacheService.evict(user.getId());
        otpService.clearResetToken(email);

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

        Set<String> permissions = new HashSet<>();
        try {
            var principal = (WebliixPrincipal) userDetailsService.loadUserByUsername(email);
            if (principal.isSuperAdmin()) {
                permissions.add("*");
            }
            if (principal.getPermissions() != null) {
                permissions.addAll(principal.getPermissions().keySet());
            }
        } catch (Exception ex) {
            log.debug("Unable to compute principal permissions: {}", ex.getMessage());
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .department(user.getDepartment())
                .bio(user.getBio())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .twoFactorEnabled(user.getTwoFactorEnabled() != null ? user.getTwoFactorEnabled() : false)
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified() != null ? user.getEmailVerified() : false)
                .roles(roleNames)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthMeResponse getAuthMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }

        String email = auth.getName();
        WebliixPrincipal principal = (WebliixPrincipal) userDetailsService.loadUserByUsername(email);
        UserProfileResponse userProfile = getCurrentUser(email);

        List<UserPermissionItemDto> permList = new ArrayList<>();
        if (principal.isSuperAdmin()) {
            List<Permission> allPerms = permissionRepository.findAll();
            for (Permission p : allPerms) {
                permList.add(new UserPermissionItemDto(p.getCode(), ScopeType.ALL));
            }
        } else if (principal.getPermissions() != null) {
            for (Map.Entry<String, ScopeType> entry : principal.getPermissions().entrySet()) {
                permList.add(new UserPermissionItemDto(entry.getKey(), entry.getValue()));
            }
        }

        // Build server-driven navigation menu based on user permissions
        List<MenuItem> allMenuItems = menuItemRepository.findByActiveTrueOrderBySortOrderAsc();
        List<MenuItem> authorizedItems = allMenuItems.stream()
                .filter(item -> {
                    if (principal.isSuperAdmin()) {
                        return true;
                    }
                    if (item.getRequiredPermission() == null || item.getRequiredPermission().isBlank()) {
                        return true;
                    }
                    return principal.hasPermission(item.getRequiredPermission());
                })
                .collect(Collectors.toList());

        Map<String, NavigationItemDto> itemMap = new LinkedHashMap<>();
        for (MenuItem mi : authorizedItems) {
            NavigationItemDto dto = NavigationItemDto.builder()
                    .key(mi.getKey())
                    .label(mi.getLabel())
                    .path(mi.getPath())
                    .icon(mi.getIcon())
                    .module(mi.getModule())
                    .requiredPermission(mi.getRequiredPermission())
                    .sortOrder(mi.getSortOrder())
                    .parentKey(mi.getParentKey())
                    .children(new ArrayList<>())
                    .build();
            itemMap.put(mi.getKey(), dto);
        }

        List<NavigationItemDto> rootNavigation = new ArrayList<>();
        for (NavigationItemDto dto : itemMap.values()) {
            if (dto.getParentKey() != null && !dto.getParentKey().isBlank() && itemMap.containsKey(dto.getParentKey())) {
                itemMap.get(dto.getParentKey()).getChildren().add(dto);
            } else {
                rootNavigation.add(dto);
            }
        }

        List<String> portalAccess = new ArrayList<>();
        if (principal.isSuperAdmin()) {
            portalAccess.addAll(List.of("CRM", "EMPLOYEE", "CLIENT", "ADMIN"));
        } else {
            if (principal.hasPermission("DASHBOARD_VIEW")) {
                portalAccess.add("CRM");
            }
            if (principal.hasPermission("EMPLOYEE_PORTAL_ACCESS") || principal.getRoles().contains("EMPLOYEE")) {
                portalAccess.add("EMPLOYEE");
            }
            if (principal.hasPermission("CLIENT_PORTAL_ACCESS") || principal.getRoles().contains("CLIENT") || principal.getRoles().contains("USER")) {
                portalAccess.add("CLIENT");
            }
        }

        return AuthMeResponse.builder()
                .id(userProfile.getId())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .email(userProfile.getEmail())
                .phone(userProfile.getPhone())
                .jobTitle(userProfile.getJobTitle())
                .department(userProfile.getDepartment())
                .bio(userProfile.getBio())
                .timezone(userProfile.getTimezone())
                .language(userProfile.getLanguage())
                .twoFactorEnabled(userProfile.getTwoFactorEnabled())
                .enabled(userProfile.getEnabled())
                .emailVerified(userProfile.getEmailVerified())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .user(userProfile)
                .roles(principal.getRoles())
                .permissions(permList)
                .navigation(rootNavigation)
                .portalAccess(portalAccess)
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName().trim());
        if (request.getLastName() != null) user.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle().trim());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment().trim());
        if (request.getBio() != null) user.setBio(request.getBio().trim());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone().trim());
        if (request.getLanguage() != null) user.setLanguage(request.getLanguage().trim());
        if (request.getTwoFactorEnabled() != null) user.setTwoFactorEnabled(request.getTwoFactorEnabled());

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
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        userPermissionCacheService.evict(user.getId());
        log.info("Password changed and token_version bumped to {} for user {}", user.getTokenVersion(), email);
    }
}
