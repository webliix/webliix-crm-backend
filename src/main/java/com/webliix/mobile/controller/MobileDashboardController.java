package com.webliix.mobile.controller;

import com.webliix.security.annotation.RequirePermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mobile")
@RequirePermission(anyOf = {"EMPLOYEE_PORTAL_ACCESS", "DASHBOARD_VIEW"})
public class MobileDashboardController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> resp = new HashMap<>();
        // Mobile-first compact response
        resp.put("summary", new HashMap<>());
        resp.put("recentActivities", new java.util.ArrayList<>());
        resp.put("notifications", new java.util.ArrayList<>());
        return ResponseEntity.ok(resp);
    }
}
