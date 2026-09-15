package com.webliix.audit.repository;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByActionAndCreatedAtBetween(AuditAction action, LocalDateTime start, LocalDateTime end);

    long countByStatusIgnoreCaseAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    long countByActionAndStatusIgnoreCaseAndCreatedAtBetween(AuditAction action, String status, LocalDateTime start, LocalDateTime end);

    List<AuditLog> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByCreatedAtBefore(LocalDateTime timestamp);
}
