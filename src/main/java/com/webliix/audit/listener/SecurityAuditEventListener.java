package com.webliix.audit.listener;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class SecurityAuditEventListener {

    private final AuditService auditService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        HttpServletRequest request = resolveHttpServletRequest();
        auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "SUCCESS", request);
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        HttpServletRequest request = resolveHttpServletRequest();
        auditService.record(AuditAction.LOGIN, AuditModule.AUTH, null, null, null, null, "FAILED", request);
    }

    @EventListener
    public void onAuthorizationFailure(AuthorizationFailureEvent event) {
        HttpServletRequest request = resolveHttpServletRequest();
        auditService.record(AuditAction.REJECT, AuditModule.AUTH, null, null, null, null, "ACCESS_DENIED", request);
    }

    private HttpServletRequest resolveHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }
}
