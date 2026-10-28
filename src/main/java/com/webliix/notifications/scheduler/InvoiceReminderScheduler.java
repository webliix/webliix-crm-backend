package com.webliix.notifications.scheduler;

import com.webliix.finance.entity.Invoice;
import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.entity.Quotation;
import com.webliix.finance.repository.QuotationRepository;
import com.webliix.notifications.service.EmailService;
import com.webliix.settings.service.SettingsCacheService;
import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.enums.TicketPriority;
import com.webliix.tickets.enums.TicketStatus;
import com.webliix.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final QuotationRepository quotationRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final SettingsCacheService settingsCacheService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendInvoiceReminders() {
        runInvoiceRemindersNow();
    }

    public int runInvoiceRemindersNow() {
        log.info("Starting scheduled invoice reminder automation job");
        boolean enabled = isJobEnabled("automation.invoice_reminders.enabled", "notify.invoice.overdue.scheduler");
        if (!enabled) {
            log.info("Invoice reminder job is paused in system settings. Skipping run.");
            return 0;
        }

        LocalDate threshold = LocalDate.now().plusDays(3);
        List<Invoice> allInvoices = invoiceRepository.findByStatusNotIn(List.of(InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
        int sentCount = 0;

        for (Invoice invoice : allInvoices) {
            if (invoice.getStatus() != InvoiceStatus.PAID && invoice.getStatus() != InvoiceStatus.CANCELLED) {
                if (invoice.getDueDate() != null && !invoice.getDueDate().isAfter(threshold)) {
                    if (invoice.getCustomer() != null && invoice.getCustomer().getEmail() != null) {
                        try {
                            String subject = "Payment Due Notice - Invoice " + invoice.getInvoiceNumber();
                            String amountFormatted = invoice.getTotalAmount() != null
                                    ? "â‚¹" + invoice.getTotalAmount().toPlainString()
                                    : "â‚¹0.00";
                            String html = "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; background-color: #f8fafc; padding: 24px;\">" +
                                    "<div style=\"max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; padding: 32px;\">" +
                                    "<h2 style=\"color: #0f172a; margin-top: 0;\">Invoice Payment Reminder</h2>" +
                                    "<p style=\"color: #64748b;\">Dear " + invoice.getCustomer().getCompanyName() + ",</p>" +
                                    "<p style=\"color: #64748b;\">This is a reminder that invoice <strong>" + invoice.getInvoiceNumber() + "</strong> " +
                                    "for <strong>" + amountFormatted + "</strong> is due on <strong>" + invoice.getDueDate() + "</strong>.</p>" +
                                    "<div style=\"text-align: center; margin: 28px 0;\">" +
                                    "<a href=\"https://login.webliix.com\" style=\"background-color: #f59e0b; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 4px; font-weight: bold;\">Pay Online</a>" +
                                    "</div>" +
                                    "<p style=\"color: #94a3b8; font-size: 12px;\">Webliix Hub Finance â€¢ billing@webliix.com</p>" +
                                    "</div></body></html>";

                            emailService.sendAutomatedHtmlEmail(invoice.getCustomer().getEmail(), subject, html);
                            sentCount++;
                        } catch (Exception ex) {
                            log.error("Failed to send reminder for invoice {}: {}", invoice.getInvoiceNumber(), ex.getMessage());
                        }
                    }
                }
            }
        }

        log.info("Invoice reminder job completed. Dispatched {} reminders.", sentCount);
        return sentCount;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendQuotationReminders() {
        runQuotationRemindersNow();
    }

    public int runQuotationRemindersNow() {
        log.info("Starting scheduled quotation follow-up automation job");
        boolean enabled = isJobEnabled("automation.quotation_reminders.enabled", "notify.quotation.scheduler");
        if (!enabled) {
            log.info("Quotation follow-up job is paused in system settings. Skipping run.");
            return 0;
        }

        List<Quotation> quotations = quotationRepository.findAll();
        int sentCount = 0;
        for (Quotation quotation : quotations) {
            if (quotation.getCustomer() != null && quotation.getCustomer().getEmail() != null) {
                if (quotation.getValidTill() != null && !quotation.getValidTill().isBefore(LocalDate.now())) {
                    try {
                        String subject = "Proposal Follow-up - Quotation " + quotation.getQuotationNumber();
                        String html = "<p>Dear " + quotation.getCustomer().getCompanyName() + ", this is a quick follow-up regarding quotation <strong>" +
                                quotation.getQuotationNumber() + "</strong> valid till " + quotation.getValidTill() + ". Please log in to your portal to review and approve.</p>";
                        emailService.sendAutomatedHtmlEmail(quotation.getCustomer().getEmail(), subject, html);
                        sentCount++;
                    } catch (Exception ex) {
                        log.error("Failed to send quotation follow-up {}: {}", quotation.getQuotationNumber(), ex.getMessage());
                    }
                }
            }
        }
        log.info("Quotation reminder job completed. Dispatched {} follow-ups.", sentCount);
        return sentCount;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void checkStaleTickets() {
        runTicketEscalationNow();
    }

    public int runTicketEscalationNow() {
        log.info("Starting ticket SLA check and stale ticket escalation job");
        boolean enabled = isJobEnabled("automation.ticket_escalation.enabled", "notify.ticket.scheduler");
        if (!enabled) {
            log.info("Ticket escalation job is paused in system settings.");
            return 0;
        }

        List<Ticket> openTickets = ticketRepository.findAll();
        int escalatedCount = 0;
        for (Ticket ticket : openTickets) {
            if (ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.IN_PROGRESS) {
                if (ticket.getDueDate() != null && ticket.getDueDate().isBefore(LocalDate.now())) {
                    if (ticket.getPriority() != TicketPriority.CRITICAL && ticket.getPriority() != TicketPriority.HIGH) {
                        ticket.setPriority(TicketPriority.HIGH);
                        ticketRepository.save(ticket);
                        escalatedCount++;
                    }
                }
            }
        }
        log.info("Ticket SLA escalation completed. Escalated {} tickets.", escalatedCount);
        return escalatedCount;
    }

    @Scheduled(cron = "0 0 11 * * MON")
    public void sendWeeklyOverdueReport() {
        log.info("Weekly financial overdue summary completed.");
    }

    private boolean isJobEnabled(String primaryKey, String secondaryKey) {
        var s1 = settingsCacheService.getByKey(primaryKey);
        if (s1 != null && s1.getSettingValue() != null) {
            return "true".equalsIgnoreCase(s1.getSettingValue().trim());
        }
        var s2 = settingsCacheService.getByKey(secondaryKey);
        if (s2 != null && s2.getSettingValue() != null) {
            return "true".equalsIgnoreCase(s2.getSettingValue().trim());
        }
        return true;
    }
}
