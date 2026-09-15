package com.webliix.notifications.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceReminderScheduler {

    // TODO: Implement invoice repository injection
    // private final InvoiceRepository invoiceRepository;
    // private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendInvoiceReminders() {
        log.info("Starting scheduled invoice reminder job");

        // TODO: Find invoices due in 3 days
        // TODO: Find overdue invoices
        // TODO: Send reminder notifications
        // TODO: Log results

        log.info("Invoice reminder job completed");
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendQuotationReminders() {
        log.info("Starting scheduled quotation reminder job");

        // TODO: Find pending quotations older than 7 days
        // TODO: Send reminder notifications
        // TODO: Log results

        log.info("Quotation reminder job completed");
    }

    @Scheduled(cron = "0 0 11 * * MON")
    public void sendWeeklyOverdueReport() {
        log.info("Starting weekly overdue report job");

        // TODO: Find all overdue invoices
        // TODO: Send summary notification
        // TODO: Log results

        log.info("Weekly overdue report job completed");
    }
}
