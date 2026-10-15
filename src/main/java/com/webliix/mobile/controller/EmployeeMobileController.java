package com.webliix.mobile.controller;

import com.webliix.security.annotation.RequirePermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mobile")
@RequirePermission(anyOf = {"EMPLOYEE_PORTAL_ACCESS", "DASHBOARD_VIEW"})
public class EmployeeMobileController {

    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> attendance() {
        return ResponseEntity.ok(new HashMap<>());
    }

    @PostMapping("/check-in")
    public ResponseEntity<Map<String, String>> checkIn() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/check-out")
    public ResponseEntity<Map<String, String>> checkOut() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/leave")
    public ResponseEntity<Map<String, Object>> leave() {
        return ResponseEntity.ok(new HashMap<>());
    }

    @GetMapping("/payroll")
    public ResponseEntity<Map<String, Object>> payroll() {
        return ResponseEntity.ok(new HashMap<>());
    }
}
