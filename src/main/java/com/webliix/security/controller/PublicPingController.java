package com.webliix.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight public health/ping controller for uptime monitoring and keep-alive pings.
 * Deliberately performs ZERO database queries to allow serverless PostgreSQL (Neon)
 * to scale to zero compute units when idle.
 */
@RestController
@RequestMapping({"/api/v1/public", "/api/public"})
public class PublicPingController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "webliix-backend"
        ));
    }
}
