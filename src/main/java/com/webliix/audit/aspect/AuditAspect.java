package com.webliix.audit.aspect;

import com.webliix.audit.annotation.Auditable;
import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        HttpServletRequest request = resolveHttpServletRequest();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            auditService.record(auditable.action(), auditable.module(), auditable.entityType(), resolveEntityId(joinPoint, null, auditable), null, null, "FAILED", request);
            throw throwable;
        }

        String entityType = StringUtils.hasText(auditable.entityType()) ? auditable.entityType() : resolveEntityType(joinPoint, result);
        String entityId = resolveEntityId(joinPoint, result, auditable);
        Object oldValue = captureOldValue(joinPoint, auditable.action());
        Object newValue = result;
        String status = StringUtils.hasText(auditable.status()) ? auditable.status() : "SUCCESS";

        auditService.record(auditable.action(), auditable.module(), entityType, entityId, oldValue, newValue, status, request);
        return result;
    }

    private Object captureOldValue(ProceedingJoinPoint joinPoint, AuditAction action) {
        if (action == AuditAction.UPDATE || action == AuditAction.DELETE) {
            return joinPoint.getArgs();
        }
        return null;
    }

    private HttpServletRequest resolveHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String resolveEntityType(ProceedingJoinPoint joinPoint, Object result) {
        if (result != null) {
            return result.getClass().getSimpleName();
        }
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            return args[0].getClass().getSimpleName();
        }
        return null;
    }

    private String resolveEntityId(ProceedingJoinPoint joinPoint, Object result, Auditable auditable) {
        if (StringUtils.hasText(auditable.entityId())) {
            return auditable.entityId();
        }

        Optional<String> idFromResult = extractId(result);
        if (idFromResult.isPresent()) {
            return idFromResult.get();
        }

        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                continue;
            }
            Optional<String> idFromArg = extractId(arg);
            if (idFromArg.isPresent()) {
                return idFromArg.get();
            }
        }

        return null;
    }

    private Optional<String> extractId(Object target) {
        if (target == null) {
            return Optional.empty();
        }

        try {
            Method getIdMethod = target.getClass().getMethod("getId");
            Object value = getIdMethod.invoke(target);
            if (value != null) {
                return Optional.of(value.toString());
            }
        } catch (Exception ignored) {
        }

        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object value = idField.get(target);
            if (value != null) {
                return Optional.of(value.toString());
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }
}
