package com.webliix.security.aspect;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.evaluator.PermissionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Authoritative AOP aspect for evaluating RBAC authorization via {@link RequirePermission}.
 * Intercepts method-level and class-level permission annotations across all controller
 * and service beans, enforcing role-based permissions and recording required permissions
 * on the current request for audit logging and access-denied handling.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionAspect {

    private final PermissionEvaluator permissionEvaluator;

    @Before("@annotation(com.webliix.security.annotation.RequirePermission) || @within(com.webliix.security.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 1. Method-level annotation takes precedence over class-level
        RequirePermission annotation = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        if (annotation == null && joinPoint.getTarget() != null) {
            annotation = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequirePermission.class);
        }

        if (annotation == null) {
            return;
        }

        // 2. Collect all required permission codes from value() and anyOf()
        List<String> requiredCodes = new ArrayList<>();
        if (StringUtils.hasText(annotation.value())) {
            requiredCodes.add(annotation.value().trim());
        }
        if (annotation.anyOf() != null) {
            for (String code : annotation.anyOf()) {
                if (StringUtils.hasText(code)) {
                    requiredCodes.add(code.trim());
                }
            }
        }

        boolean allowed;
        String requiredDisplay;

        if (requiredCodes.isEmpty()) {
            // Empty annotation defaults to requiring an authenticated user
            allowed = permissionEvaluator.has("");
            requiredDisplay = "AUTHENTICATED";
        } else {
            allowed = permissionEvaluator.hasAny(requiredCodes.toArray(new String[0]));
            requiredDisplay = String.join(", ", requiredCodes);
        }

        if (!allowed) {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                request.setAttribute("REQUIRED_PERMISSION", requiredDisplay);
            }
            log.warn("Access denied for method {}. Required permission(s): {}", method.getName(), requiredDisplay);
            throw new AccessDeniedException("Access denied. Required permission: " + requiredDisplay);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
