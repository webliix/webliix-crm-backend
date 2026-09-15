package com.webliix.audit.listener;

import com.webliix.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditCleanupScheduler {

    private final AuditService auditService;

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldAuditLogs() {
        auditService.deleteLogsOlderThanDays(365);
    }
}
