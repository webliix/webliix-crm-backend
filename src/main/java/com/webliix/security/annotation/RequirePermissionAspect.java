package com.webliix.security.annotation;

import com.webliix.security.evaluator.PermissionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

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

        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }

        if (annotation == null) {
            return;
        }

        String primary = annotation.value();
        String[] anyOf = annotation.anyOf();

        boolean allowed = false;
        String required = primary;

        if (primary != null && !primary.isBlank()) {
            allowed = permissionEvaluator.has(primary);
            required = primary;
        } else if (anyOf != null && anyOf.length > 0) {
            allowed = permissionEvaluator.hasAny(anyOf);
            required = Arrays.toString(anyOf);
        } else {
            // Empty annotation defaults to requiring authentication
            allowed = permissionEvaluator.has("");
        }

        if (!allowed) {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                request.setAttribute("REQUIRED_PERMISSION", required);
            }
            log.warn("Access denied for method {}. Required permission: {}", method.getName(), required);
            throw new AccessDeniedException("Access denied. Required permission: " + required);
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
