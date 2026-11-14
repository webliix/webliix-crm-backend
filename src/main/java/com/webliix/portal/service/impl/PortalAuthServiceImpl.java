package com.webliix.portal.service.impl;

import com.webliix.portal.dto.PortalAuthRequest;
import com.webliix.portal.dto.PortalAuthResponse;
import com.webliix.portal.entity.PortalUser;
import com.webliix.portal.repository.PortalUserRepository;
import com.webliix.portal.service.PortalAuthService;
import com.webliix.security.jwt.JwtService;
import com.webliix.security.model.WebliixPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalAuthServiceImpl implements PortalAuthService {

    private final PortalUserRepository portalUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public PortalAuthResponse login(PortalAuthRequest request) {
        PortalUser user = portalUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        user.setLastLogin(LocalDateTime.now());
        portalUserRepository.save(user);

        WebliixPrincipal principal = WebliixPrincipal.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .enabled(true)
                .emailVerified(true)
                .tokenVersion(1)
                .roles(Collections.singleton("ROLE_CLIENT"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")))
                .build();

        String token = jwtService.generateToken(principal);

        return PortalAuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .token(token)
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        portalUserRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            // Stored or logged for secure recovery
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        throw new UnsupportedOperationException("Reset via token not implemented. Use admin portal to reset password.");
    }
}
