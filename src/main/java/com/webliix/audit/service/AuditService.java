package com.webliix.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.audit.dto.AuditDashboardResponse;
import com.webliix.audit.dto.AuditLogResponse;
import com.webliix.audit.dto.AuditSearchRequest;
import com.webliix.audit.dto.UserActivityResponse;
import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditLog;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.repository.AuditLogRepository;
import com.webliix.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditLog record(AuditAction action,
                           AuditModule module,
                           String entityType,
                           String entityId,
                           Object oldValue,
                           Object newValue,
                           String status,
                           HttpServletRequest request) {

        String username = resolveUsername(request);
        Long userId = resolveUserId(username);

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .module(module)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .ipAddress(request != null ? request.getRemoteAddr() : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> search(AuditSearchRequest request) {
        Pageable pageable = PageRequest.of(Math.max(0, Objects.requireNonNullElse(request.getPage(), 0)), Math.max(1, Objects.requireNonNullElse(request.getSize(), 25)));
        Specification<AuditLog> specification = buildSpecification(request);
        return auditLogRepository.findAll(specification, pageable).map(this::mapToResponse);
    }

    public List<UserActivityResponse> getUserActivity(Long userId) {
        return auditLogRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(log -> UserActivityResponse.builder()
                        .action(log.getAction().name())
                        .module(log.getModule().name())
                        .entityId(log.getEntityId())
                        .time(log.getCreatedAt())
                        .build())
                .toList();
    }

    public AuditDashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfDay.plusDays(1);

        long todayActivities = auditLogRepository.countByCreatedAtBetween(startOfDay, startOfTomorrow);
        long failedActivities = auditLogRepository.countByStatusIgnoreCaseAndCreatedAtBetween("FAILED", startOfDay, startOfTomorrow);
        long loginsToday = auditLogRepository.countByActionAndCreatedAtBetween(AuditAction.LOGIN, startOfDay, startOfTomorrow);
        long exportsToday = auditLogRepository.countByActionAndCreatedAtBetween(AuditAction.EXPORT, startOfDay, startOfTomorrow);

        return AuditDashboardResponse.builder()
                .todayActivities(todayActivities)
                .failedActivities(failedActivities)
                .loginsToday(loginsToday)
                .exportsToday(exportsToday)
                .build();
    }

    public void deleteLogsOlderThanDays(int days) {
        auditLogRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(days));
    }

    private String resolveUsername(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            if (principal instanceof String username) {
                return username;
            }
        }
        if (request != null && request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return null;
    }

    private Long resolveUserId(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByEmail(username)
                .map(user -> user.getId())
                .orElse(null);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .module(auditLog.getModule())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .status(auditLog.getStatus())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private Specification<AuditLog> buildSpecification(AuditSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getModule())) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("module"), AuditModule.valueOf(request.getModule().toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (StringUtils.hasText(request.getAction())) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("action"), AuditAction.valueOf(request.getAction().toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), request.getUserId()));
            }
            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), request.getStatus().toLowerCase()));
            }
            if (StringUtils.hasText(request.getEntityType())) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), request.getEntityType()));
            }
            if (StringUtils.hasText(request.getEntityId())) {
                predicates.add(criteriaBuilder.equal(root.get("entityId"), request.getEntityId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
