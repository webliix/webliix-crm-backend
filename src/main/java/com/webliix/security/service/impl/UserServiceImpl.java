package com.webliix.security.service.impl;

import com.webliix.security.dto.CreateUserRequest;
import com.webliix.security.dto.UpdateAdminUserRequest;
import com.webliix.security.dto.UserProfileResponse;
import com.webliix.security.entity.Role;
import com.webliix.security.entity.User;
import com.webliix.security.repository.RoleRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.security.service.UserService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserProfileResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            users = userRepository.findAll(pageable); // In production can use specification filter
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToProfileResponse);
    }

    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists.");
        }

        String roleName = request.getRole() != null && !request.getRole().isBlank()
                ? request.getRole().trim().toUpperCase()
                : "EMPLOYEE";

        Role assignedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found"));

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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(Set.of(assignedRole))
                .build();

        User saved = userRepository.save(user);
        log.info("Super Admin created new user account: ID {}, Email {}, Role {}", saved.getId(), saved.getEmail(), roleName);
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

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String roleName = request.getRole().trim().toUpperCase();
            Role assignedRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found"));
            user.setRoles(Set.of(assignedRole));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        log.info("User ID {} updated by Super Admin", id);
        return mapToProfileResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent deletion of bootstrap super admin
        boolean isSuperAdmin = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "SUPER_ADMIN".equalsIgnoreCase(r.getName()));
        if (isSuperAdmin && userRepository.count() == 1) {
            throw new IllegalArgumentException("Cannot delete the last Super Admin account.");
        }

        userRepository.delete(user);
        log.info("User ID {} deleted by Super Admin", id);
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToProfileResponse(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of("USER");

        Set<String> permissions = new HashSet<>();
        if (roleNames.contains("SUPER_ADMIN")) {
            permissions.addAll(Set.of(
                "DASHBOARD_VIEW", "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_DELETE", "LEADS_CONVERT",
                "CLIENTS_VIEW", "CLIENTS_CREATE", "CLIENTS_EDIT", "CLIENTS_DELETE",
                "BLOGS_VIEW", "BLOGS_CREATE", "BLOGS_EDIT", "BLOGS_DELETE",
                "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT", "PROJECTS_DELETE",
                "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT", "TICKETS_DELETE",
                "USERS_MANAGE", "USERS_DELETE", "NOTIFICATIONS_BROADCAST", "REPORTS_VIEW", "SETTINGS_VIEW"
            ));
        } else if (roleNames.contains("ADMIN")) {
            permissions.addAll(Set.of(
                "DASHBOARD_VIEW", "LEADS_VIEW", "LEADS_CREATE", "LEADS_EDIT", "LEADS_CONVERT",
                "CLIENTS_VIEW", "CLIENTS_CREATE", "CLIENTS_EDIT",
                "BLOGS_VIEW", "BLOGS_CREATE", "BLOGS_EDIT",
                "PROJECTS_VIEW", "PROJECTS_CREATE", "PROJECTS_EDIT",
                "TICKETS_VIEW", "TICKETS_CREATE", "TICKETS_EDIT",
                "USERS_MANAGE", "NOTIFICATIONS_BROADCAST", "REPORTS_VIEW"
            ));
        } else {
            permissions.addAll(Set.of("DASHBOARD_VIEW", "PROJECTS_VIEW", "TICKETS_VIEW", "PUBLIC_UPDATES_VIEW"));
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
