package com.webliix.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String requiredPermission = (String) request.getAttribute("REQUIRED_PERMISSION");
        if (requiredPermission == null && accessDeniedException != null && accessDeniedException.getMessage() != null) {
            String msg = accessDeniedException.getMessage();
            if (msg.contains("Required permission: ")) {
                requiredPermission = msg.substring(msg.indexOf("Required permission: ") + "Required permission: ".length()).trim();
            }
        }
        if (requiredPermission == null) {
            requiredPermission = "ACCESS_DENIED";
        }

        try {
            auditService.record(
                    AuditAction.ACCESS_DENIED,
                    AuditModule.AUTH,
                    null,
                    null,
                    null,
                    null,
                    "Denied at " + request.getRequestURI() + " (Required: " + requiredPermission + ")",
                    request
            );
        } catch (Exception ex) {
            log.warn("Failed to record access denied audit log: {}", ex.getMessage());
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", "FORBIDDEN");
        body.put("message", "Access denied. You do not possess the required permission.");
        body.put("requiredPermission", requiredPermission);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
