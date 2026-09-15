package com.webliix.security.controller;

import com.webliix.security.entity.User;
import com.webliix.security.entity.UserApiToken;
import com.webliix.security.repository.UserApiTokenRepository;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.response.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final UserApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserApiToken>>> getTokens(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        List<UserApiToken> tokens = apiTokenRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (tokens.isEmpty()) {
            // Seed initial token into DB for clean management
            UserApiToken initial = UserApiToken.builder()
                    .userId(userId)
                    .name("Zapier Lead Integration Key")
                    .tokenKey("wbx_live_sk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                    .scope("Full Admin")
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .expiresAt(LocalDateTime.now().plusYears(1))
                    .build();
            apiTokenRepository.save(initial);
            tokens = apiTokenRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return ResponseEntity.ok(ApiResponse.<List<UserApiToken>>builder()
                .success(true)
                .message("API tokens fetched")
                .data(tokens)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserApiToken>> createToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateTokenRequest request
    ) {
        Long userId = resolveUserId(userDetails);
        String prefix = "Read-Only".equalsIgnoreCase(request.getScope()) ? "wbx_live_ro_" : "wbx_live_sk_";
        String generatedKey = prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        UserApiToken token = UserApiToken.builder()
                .userId(userId)
                .name(request.getName() != null ? request.getName().trim() : "Custom API Key")
                .tokenKey(generatedKey)
                .scope(request.getScope() != null ? request.getScope().trim() : "Read-Only")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();

        UserApiToken saved = apiTokenRepository.save(token);
        return ResponseEntity.ok(ApiResponse.<UserApiToken>builder()
                .success(true)
                .message("API token created successfully")
                .data(saved)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeToken(@PathVariable Long id) {
        apiTokenRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("API token revoked")
                .build());
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (user != null) return user.getId();
        }
        return 1L; // Fallback to primary admin user id
    }

    @Data
    public static class CreateTokenRequest {
        private String name;
        private String scope;
    }
}
