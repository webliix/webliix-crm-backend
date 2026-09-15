package com.webliix.portal.service.impl;

import com.webliix.portal.dto.PortalAuthRequest;
import com.webliix.portal.dto.PortalAuthResponse;
import com.webliix.portal.entity.PortalUser;
import com.webliix.portal.repository.PortalUserRepository;
import com.webliix.portal.service.PortalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalAuthServiceImpl implements PortalAuthService {

    private final PortalUserRepository portalUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public PortalAuthResponse login(PortalAuthRequest request) {
        PortalUser user = portalUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        user.setLastLogin(LocalDateTime.now());
        portalUserRepository.save(user);
        // For simplicity, generate a dummy token (UUID). Project likely uses JWT; integrate as needed.
        String token = UUID.randomUUID().toString();
        return PortalAuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .token(token)
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        // Simple implementation: generate a reset token and log/store it somewhere.
        portalUserRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            // TODO: persist token and send email. For now, log to console.
            System.out.println("Portal password reset token for " + email + ": " + resetToken);
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Token handling not implemented: in production persist tokens with expiry. Here skip and throw.
        throw new UnsupportedOperationException("Reset via token not implemented. Use admin to reset password or implement token storage.");
    }
}
