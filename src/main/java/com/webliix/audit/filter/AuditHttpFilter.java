package com.webliix.audit.filter;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
@Slf4j
public class AuditHttpFilter extends OncePerRequestFilter {

    private final AuditService auditService;
    private static final Pattern ID_PATTERN = Pattern.compile("/([0-9]+)(?:/|$)");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // Only log mutating actions (POST, PUT, PATCH, DELETE) under /api/v1/
        // Do not log read-only GET/OPTIONS/HEAD requests
        if ("GET".equals(method) || "OPTIONS".equals(method) || "HEAD".equals(method)) {
            return true;
        }

        // Exclude Swagger, WebSocket, and audit query endpoints to avoid recursion
        if (!path.startsWith("/api/v1/")) {
            return true;
        }
        if (path.startsWith("/api/v1/audit") || path.startsWith("/api/v1/audit-logs")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Exception caughtException = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            caughtException = e;
            throw e;
        } finally {
            try {
                recordAudit(request, response, caughtException);
            } catch (Throwable t) {
                log.warn("Non-fatal: Failed to record audit log for {}: {}", request.getRequestURI(), t.getMessage());
            }
        }
    }

    private void recordAudit(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String path = request.getRequestURI();
        String method = request.getMethod().toUpperCase();
        int status = response.getStatus();

        String auditStatus = (ex != null || status >= 400) ? "FAILED" : "SUCCESS";
        AuditModule module = resolveModule(path);
        AuditAction action = resolveAction(method, path);
        String entityType = resolveEntityType(path);
        String entityId = resolveEntityId(path);

        auditService.record(
                action,
                module,
                entityType,
                entityId,
                null,
                method + " " + path + " -> HTTP " + status,
                auditStatus,
                request
        );
    }

    private AuditModule resolveModule(String path) {
        String p = path.toLowerCase();
        if (p.contains("/auth")) return AuditModule.AUTH;
        if (p.contains("/lead")) return AuditModule.CRM;
        if (p.contains("/customer")) return AuditModule.CUSTOMER;
        if (p.contains("/project")) return AuditModule.PROJECT;
        if (p.contains("/invoice") || p.contains("/payment") || p.contains("/quotation") || p.contains("/expense")) return AuditModule.FINANCE;
        if (p.contains("/employee") || p.contains("/department") || p.contains("/designation") || p.contains("/work-log")) return AuditModule.HR;
        if (p.contains("/payroll")) return AuditModule.PAYROLL;
        if (p.contains("/ticket")) return AuditModule.TICKET;
        if (p.contains("/notification")) return AuditModule.NOTIFICATION;
        if (p.contains("/automation")) return AuditModule.AUTOMATION;
        return AuditModule.CRM;
    }

    private AuditAction resolveAction(String method, String path) {
        String p = path.toLowerCase();
        if (p.contains("/login")) return AuditAction.LOGIN;
        if (p.contains("/logout")) return AuditAction.LOGOUT;
        if (p.contains("/approve")) return AuditAction.APPROVE;
        if (p.contains("/reject")) return AuditAction.REJECT;
        if (p.contains("/assign")) return AuditAction.ASSIGN;
        if (p.contains("/generate")) return AuditAction.GENERATE;
        if (p.contains("/export")) return AuditAction.EXPORT;

        return switch (method) {
            case "POST" -> AuditAction.CREATE;
            case "PUT", "PATCH" -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            default -> AuditAction.CREATE;
        };
    }

    private String resolveEntityType(String path) {
        String clean = path.replace("/api/v1/", "").replace("/api/", "");
        String[] parts = clean.split("/");
        if (parts.length > 0 && !parts[0].isBlank()) {
            return parts[0].toUpperCase();
        }
        return "RESOURCE";
    }

    private String resolveEntityId(String path) {
        Matcher matcher = ID_PATTERN.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
