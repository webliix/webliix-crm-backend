package com.webliix.security.service;

import com.webliix.security.dto.CreateUserRequest;
import com.webliix.security.dto.UpdateAdminUserRequest;
import com.webliix.security.dto.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserProfileResponse> getAllUsers(String search, Pageable pageable);
    UserProfileResponse createUser(CreateUserRequest request);
    UserProfileResponse updateUser(Long id, UpdateAdminUserRequest request);
    void deleteUser(Long id);
    UserProfileResponse getUserById(Long id);
}
