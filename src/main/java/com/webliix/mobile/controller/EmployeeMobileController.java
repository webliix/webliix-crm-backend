package com.webliix.mobile.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile")
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
