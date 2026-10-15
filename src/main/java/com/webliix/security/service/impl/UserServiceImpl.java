package com.webliix.security.service.impl;

import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.security.dto.CreateUserRequest;
import com.webliix.security.dto.UpdateAdminUserRequest;
import com.webliix.security.dto.UserProfileResponse;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.model.WebliixPrincipal;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.security.service.CustomUserDetailsService;
import com.webliix.security.service.UserPermissionCacheService;
import com.webliix.security.service.UserService;
import com.webliix.shared.exceptions.BusinessException;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final com.webliix.crm.customer.repository.CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionCacheService userPermissionCacheService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Page<UserProfileResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToProfileResponse);
    }

    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists.");
        }

        Set<Role> assignedRoles = new HashSet<>();
        List<String> roleInput = request.getRoles() != null && !request.getRoles().isEmpty()
                ? request.getRoles()
                : (request.getRole() != null && !request.getRole().isBlank() ? List.of(request.getRole()) : List.of("EMPLOYEE"));

        for (String rName : roleInput) {
            String normalized = rName.trim().toUpperCase();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            if ("CUSTOMER".equals(normalized) || "CLIENT".equals(normalized)) {
                normalized = "USER";
            }
            final String lookupName = normalized;
            Role role = roleRepository.findByName(lookupName)
                    .or(() -> roleRepository.findByName("ROLE_" + lookupName))
                    .orElseThrow(() -> new ResourceNotFoundException("Role " + lookupName + " not found"));
            assignedRoles.add(role);
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .enabled(true)
                .emailVerified(true)
                .tokenVersion(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(assignedRoles)
                .build();

        User saved = userRepository.save(user);
        log.info("Created new user account: ID {}, Email {}, Roles {}", saved.getId(), saved.getEmail(),
                assignedRoles.stream().map(Role::getName).collect(Collectors.joining(",")));
        return mapToProfileResponse(saved);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUser(Long id, UpdateAdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName().trim());
        if (request.getLastName() != null) user.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle().trim());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment().trim());
        if (request.getBio() != null) user.setBio(request.getBio().trim());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        if (request.getEmailVerified() != null) user.setEmailVerified(request.getEmailVerified());

        List<String> roleInput = request.getRoles() != null && !request.getRoles().isEmpty()
                ? request.getRoles()
                : (request.getRole() != null && !request.getRole().isBlank() ? List.of(request.getRole()) : null);

        if (roleInput != null) {
            Set<Role> updatedRoles = new HashSet<>();
            for (String rName : roleInput) {
                String normalized = rName.trim().toUpperCase();
                if (normalized.startsWith("ROLE_")) {
                    normalized = normalized.substring(5);
                }
                if ("CUSTOMER".equals(normalized) || "CLIENT".equals(normalized)) {
                    normalized = "USER";
                }
                final String lookupName = normalized;
                Role role = roleRepository.findByName(lookupName)
                        .or(() -> roleRepository.findByName("ROLE_" + lookupName))
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + lookupName + " not found"));
                updatedRoles.add(role);
            }
            user.setRoles(updatedRoles);
        }

        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);

        // Invalidate permissions cache
        userPermissionCacheService.evict(id);

        // Keep linked Employee record in sync if exists
        try {
            Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                if (request.getFirstName() != null) emp.setFirstName(request.getFirstName().trim());
                if (request.getLastName() != null) emp.setLastName(request.getLastName().trim());
                if (request.getPhone() != null) emp.setPhone(request.getPhone().trim());
                emp.setUpdatedAt(LocalDateTime.now());
                employeeRepository.save(emp);
                log.info("Synced updated user profile to employee record ID {}", emp.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to sync employee record for user ID {}: {}", id, e.getMessage());
        }

        // Keep linked Customer record in sync if exists
        try {
            Optional<com.webliix.crm.customer.entity.Customer> custOpt = customerRepository.findByEmail(user.getEmail());
            if (custOpt.isPresent()) {
                com.webliix.crm.customer.entity.Customer cust = custOpt.get();
                String fullName = ((request.getFirstName() != null ? request.getFirstName() : "") + " " +
                                   (request.getLastName() != null ? request.getLastName() : "")).trim();
                if (!fullName.isEmpty()) {
                    cust.setContactPerson(fullName);
                }
                if (request.getPhone() != null) {
                    cust.setPhone(request.getPhone().trim());
                }
                cust.setUpdatedAt(LocalDateTime.now());
                customerRepository.save(cust);
                log.info("Synced updated user profile to customer record ID {}", cust.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to sync customer record for user email {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("User ID {} updated. Token version bumped to {}", id, user.getTokenVersion());
        return mapToProfileResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent deletion of bootstrap super admin
        boolean isSuperAdmin = user.getRoles() != null && user.getRoles().stream()
                .filter(Objects::nonNull)
                .anyMatch(r -> Boolean.TRUE.equals(r.getIsSuperAdmin()) || "SUPER_ADMIN".equalsIgnoreCase(r.getName()));
        if (isSuperAdmin) {
            long superAdminCount = userRepository.countActiveSuperAdmins();
            if (superAdminCount <= 1) {
                throw new BusinessException("Cannot delete the last active Super Admin account.");
            }
        }

        userPermissionCacheService.evict(id);
        userRepository.delete(user);
        log.info("User ID {} deleted", id);
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToProfileResponse(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        Set<String> roleNames = (user.getRoles() != null && !user.getRoles().isEmpty())
                ? user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(r -> {
                        String name = r.getName();
                        if (name == null) return "USER";
                        return name.startsWith("ROLE_") ? name.substring(5).toUpperCase() : name.toUpperCase();
                    })
                    .collect(Collectors.toSet())
                : Set.of("USER");

        Set<String> permissions = new HashSet<>();
        try {
            var principal = (WebliixPrincipal) customUserDetailsService.loadUserByUsername(user.getEmail());
            if (principal.isSuperAdmin()) {
                permissions.add("*");
            }
            if (principal.getPermissions() != null) {
                permissions.addAll(principal.getPermissions().keySet());
            }
        } catch (Exception ex) {
            log.debug("Unable to compute principal permissions for user {}: {}", user.getEmail(), ex.getMessage());
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
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .roles(roleNames)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
