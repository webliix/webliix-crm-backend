package com.webliix.portal.controller;

import com.webliix.portal.dto.PortalAuthRequest;
import com.webliix.portal.dto.PortalAuthResponse;
import com.webliix.portal.service.PortalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {

    private final PortalAuthService portalAuthService;

    @PostMapping("/login")
    public ResponseEntity<PortalAuthResponse> login(@RequestBody PortalAuthRequest request) {
        return ResponseEntity.ok(portalAuthService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        portalAuthService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam("token") String token,
                                             @RequestParam("password") String password) {
        portalAuthService.resetPassword(token, password);
        return ResponseEntity.noContent().build();
    }
}
