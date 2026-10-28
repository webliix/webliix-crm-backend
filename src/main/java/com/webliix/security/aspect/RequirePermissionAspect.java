package com.webliix.security.aspect;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.evaluator.PermissionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class RequirePermissionAspect {

    private final PermissionEvaluator permissionEvaluator;

    @Around("@annotation(requirePermission)")
    public Object checkMethodPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        enforce(requirePermission);
        return joinPoint.proceed();
    }

    @Around("@within(requirePermission) && !@annotation(com.webliix.security.annotation.RequirePermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        enforce(requirePermission);
        return joinPoint.proceed();
    }

    private void enforce(RequirePermission requirePermission) {
        if (requirePermission == null) {
            return;
        }

        List<String> requiredCodes = new ArrayList<>();
        if (StringUtils.hasText(requirePermission.value())) {
            requiredCodes.add(requirePermission.value().trim());
        }
        if (requirePermission.anyOf() != null) {
            for (String code : requirePermission.anyOf()) {
                if (StringUtils.hasText(code)) {
                    requiredCodes.add(code.trim());
                }
            }
        }

        if (requiredCodes.isEmpty()) {
            return;
        }

        boolean authorized = permissionEvaluator.hasAny(requiredCodes.toArray(new String[0]));
        if (!authorized) {
            log.warn("Access denied: user lacks required permissions {}", requiredCodes);
            throw new AccessDeniedException("Access denied: insufficient permissions to perform this action");
        }
    }
}
