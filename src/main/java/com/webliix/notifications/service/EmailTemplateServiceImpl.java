package com.webliix.notifications.service;

import com.webliix.notifications.dto.*;
import com.webliix.notifications.entity.EmailTemplate;
import com.webliix.notifications.repository.EmailTemplateRepository;
import com.webliix.settings.entity.SystemSetting;
import com.webliix.settings.repository.SystemSettingRepository;
import com.webliix.settings.service.SettingsCacheService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailService emailService;
    private final SystemSettingRepository systemSettingRepository;
    private final SettingsCacheService settingsCacheService;

    private static final Map<String, TemplateMeta> DEFAULT_TEMPLATES = new LinkedHashMap<>();

    static {
        DEFAULT_TEMPLATES.put("customer-welcome", new TemplateMeta(
                "Welcome to Webliix Hub - Client Portal Access",
                "Sent to client contact when converted from a lead or registered",
                "{{NAME}}, {{EMAIL}}, {{PASSWORD}}, {{LOGIN_URL}}"
        ));
        DEFAULT_TEMPLATES.put("employee-welcome", new TemplateMeta(
                "Welcome to Webliix Team - Employee Portal Credentials",
                "Sent to employee when staff account is provisioned",
                "{{NAME}}, {{EMAIL}}, {{PASSWORD}}, {{DESIGNATION}}, {{DEPARTMENT}}, {{LOGIN_URL}}"
        ));
        DEFAULT_TEMPLATES.put("email-verification-otp", new TemplateMeta(
                "Webliix Hub - Email Verification Code",
                "Sent during user sign up to verify email identity",
                "{{NAME}}, {{OTP}}, {{EXPIRATION_MINUTES}}"
        ));
        DEFAULT_TEMPLATES.put("forgot-password-otp", new TemplateMeta(
                "Webliix Hub - Password Reset Code",
                "Sent when user requests account recovery code",
                "{{NAME}}, {{OTP}}, {{EXPIRATION_MINUTES}}"
        ));
        DEFAULT_TEMPLATES.put("password-reset-success", new TemplateMeta(
                "Webliix Hub - Password Reset Successful",
                "Confirmation email that password was changed",
                "{{NAME}}"
        ));
        DEFAULT_TEMPLATES.put("invoice-generated", new TemplateMeta(
                "New Invoice Issued - Webliix Hub",
                "Sent to customer when billing generates an invoice",
                "{{CLIENT_NAME}}, {{INVOICE_NUMBER}}, {{AMOUNT}}, {{DUE_DATE}}, {{PORTAL_URL}}"
        ));
        DEFAULT_TEMPLATES.put("invoice-paid", new TemplateMeta(
                "Payment Received & Confirmed - Webliix Hub",
                "Official receipt sent when client invoice is paid",
                "{{CLIENT_NAME}}, {{INVOICE_NUMBER}}, {{AMOUNT}}, {{PAID_DATE}}"
        ));
        DEFAULT_TEMPLATES.put("lead-created", new TemplateMeta(
                "New Inbound Lead Received - Webliix CRM",
                "Notification to sales team when website inquiry is submitted",
                "{{NAME}}, {{EMAIL}}, {{PHONE}}, {{SOURCE}}"
        ));
        DEFAULT_TEMPLATES.put("lead-inquiry-acknowledgment", new TemplateMeta(
                "We Received Your Inquiry - Webliix Team",
                "Automated reply sent to visitor after contacting sales",
                "{{NAME}}, {{INQUIRY_DATE}}"
        ));
        DEFAULT_TEMPLATES.put("payroll-generated", new TemplateMeta(
                "Monthly Salary Slip Available - Webliix HR",
                "Sent to employee when monthly payroll is processed",
                "{{EMPLOYEE_NAME}}, {{MONTH}}, {{YEAR}}, {{NET_SALARY}}"
        ));
        DEFAULT_TEMPLATES.put("quotation-sent", new TemplateMeta(
                "Quotation Proposal - Webliix Hub",
                "Formal estimate proposal sent to client",
                "{{CLIENT_NAME}}, {{QUOTATION_NUMBER}}, {{TOTAL_AMOUNT}}"
        ));
        DEFAULT_TEMPLATES.put("system-notification", new TemplateMeta(
                "Webliix Hub Notification",
                "Generic system broadcast template",
                "{{TITLE}}, {{MESSAGE}}"
        ));
        DEFAULT_TEMPLATES.put("ticket-created", new TemplateMeta(
                "Support Ticket Opened - Webliix Helpdesk",
                "Sent to client and support desk when new ticket is opened",
                "{{TICKET_NUMBER}}, {{TITLE}}, {{CLIENT_NAME}}"
        ));
        DEFAULT_TEMPLATES.put("ticket-assigned", new TemplateMeta(
                "Support Ticket Assigned to You - Webliix Helpdesk",
                "Sent to staff engineer when ticket is assigned",
                "{{TICKET_NUMBER}}, {{TITLE}}, {{STAFF_NAME}}"
        ));
        DEFAULT_TEMPLATES.put("ticket-resolved", new TemplateMeta(
                "Support Ticket Resolved - Webliix Helpdesk",
                "Sent to client and team when ticket issue is resolved",
                "{{CLIENT_NAME}}, {{TICKET_NUMBER}}, {{TITLE}}, {{STAFF_NAME}}, {{MESSAGE}}"
        ));
        DEFAULT_TEMPLATES.put("invoice-reminder", new TemplateMeta(
                "Invoice Payment Due Reminder - Webliix Hub",
                "Sent to client when invoice is approaching due date or overdue",
                "{{CLIENT_NAME}}, {{INVOICE_NUMBER}}, {{AMOUNT}}, {{DUE_DATE}}"
        ));
        DEFAULT_TEMPLATES.put("job-application-received", new TemplateMeta(
                "Application Received - Careers at Webliix",
                "Automated confirmation sent to candidate upon submitting resume",
                "{{NAME}}, {{DESIGNATION}}, {{DEPARTMENT}}"
        ));
        DEFAULT_TEMPLATES.put("project-status-updated", new TemplateMeta(
                "Project Milestone Update - Webliix Hub",
                "Sent to client when engineering logs a project milestone",
                "{{CLIENT_NAME}}, {{TITLE}}, {{MESSAGE}}"
        ));
        DEFAULT_TEMPLATES.put("newsletter-welcome", new TemplateMeta(
                "Welcome to Webliix Insights & Architecture",
                "Sent to subscriber upon joining the newsletter list",
                "{{EMAIL}}, {{NAME}}"
        ));
        DEFAULT_TEMPLATES.put("welcome-email", new TemplateMeta(
                "Welcome to Webliix Hub - Getting Started",
                "Platform onboarding overview sent to newly provisioned users",
                "{{NAME}}, {{EMAIL}}, {{LOGIN_URL}}"
        ));
    }

    private static class TemplateMeta {
        final String defaultSubject;
        final String description;
        final String sampleVariables;

        TemplateMeta(String defaultSubject, String description, String sampleVariables) {
            this.defaultSubject = defaultSubject;
            this.description = description;
            this.sampleVariables = sampleVariables;
        }
    }

    @PostConstruct
    public void init() {
        seedTemplatesIfEmpty();
    }

    public synchronized void seedTemplatesIfEmpty() {
        for (Map.Entry<String, TemplateMeta> entry : DEFAULT_TEMPLATES.entrySet()) {
            String key = entry.getKey();
            TemplateMeta meta = entry.getValue();
            if (emailTemplateRepository.findByTemplateKey(key).isEmpty()) {
                String html = loadClasspathTemplate(key);
                if (html == null || html.isBlank()) {
                    html = buildFallbackTemplateHtml(meta.defaultSubject, meta.description);
                }
                EmailTemplate template = EmailTemplate.builder()
                        .templateKey(key)
                        .subject(meta.defaultSubject)
                        .htmlContent(html)
                        .description(meta.description)
                        .sampleVariables(meta.sampleVariables)
                        .isActive(true)
                        .version(1)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                emailTemplateRepository.save(template);
                log.info("Seeded default email template: {}", key);
            }
        }
    }

    @Override
    @Transactional
    public List<EmailTemplateResponse> getAllTemplates() {
        seedTemplatesIfEmpty();
        List<EmailTemplate> list = emailTemplateRepository.findAll();
        list.sort(Comparator.comparing(EmailTemplate::getTemplateKey));
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateById(Long id) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));
        return toResponse(t);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateByKey(String templateKey) {
        EmailTemplate t = emailTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + templateKey));
        return toResponse(t);
    }

    @Override
    @Transactional
    public EmailTemplateResponse updateTemplate(Long id, UpdateEmailTemplateRequest request) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));

        t.setSubject(request.getSubject());
        t.setHtmlContent(request.getHtmlContent());
        if (request.getPlainTextContent() != null) {
            t.setPlainTextContent(request.getPlainTextContent());
        }
        if (request.getIsActive() != null) {
            t.setIsActive(request.getIsActive());
        }
        if (request.getDescription() != null) {
            t.setDescription(request.getDescription());
        }
        t.setVersion(t.getVersion() != null ? t.getVersion() + 1 : 1);
        t.setUpdatedAt(LocalDateTime.now());

        EmailTemplate saved = emailTemplateRepository.save(t);
        log.info("Updated email template: {} (version {})", saved.getTemplateKey(), saved.getVersion());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EmailTemplateResponse toggleTemplate(Long id) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));
        t.setIsActive(!Boolean.TRUE.equals(t.getIsActive()));
        t.setUpdatedAt(LocalDateTime.now());
        return toResponse(emailTemplateRepository.save(t));
    }

    @Override
    public String previewTemplate(Long id, PreviewEmailTemplateRequest request) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));

        String content = request != null && request.getHtmlContent() != null && !request.getHtmlContent().isBlank()
                ? request.getHtmlContent()
                : t.getHtmlContent();

        Map<String, String> vars = request != null && request.getVariables() != null
                ? request.getVariables()
                : getDefaultSampleVariables(t.getTemplateKey());

        for (Map.Entry<String, String> e : vars.entrySet()) {
            content = content.replace("{{" + e.getKey() + "}}", e.getValue())
                             .replace("{{" + e.getKey().toUpperCase() + "}}", e.getValue())
                             .replace("{{" + e.getKey().toLowerCase() + "}}", e.getValue());
        }
        return content;
    }

    @Override
    public boolean testSendTemplate(Long id, TestSendEmailTemplateRequest request) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject()
                : "[TEST] " + t.getSubject();

        String htmlContent = request.getHtmlContent() != null && !request.getHtmlContent().isBlank()
                ? request.getHtmlContent()
                : t.getHtmlContent();

        Map<String, String> vars = request.getVariables() != null
                ? request.getVariables()
                : getDefaultSampleVariables(t.getTemplateKey());

        for (Map.Entry<String, String> e : vars.entrySet()) {
            htmlContent = htmlContent.replace("{{" + e.getKey() + "}}", e.getValue())
                                     .replace("{{" + e.getKey().toUpperCase() + "}}", e.getValue())
                                     .replace("{{" + e.getKey().toLowerCase() + "}}", e.getValue());
            subject = subject.replace("{{" + e.getKey() + "}}", e.getValue())
                             .replace("{{" + e.getKey().toUpperCase() + "}}", e.getValue());
        }

        try {
            emailService.sendAutomatedHtmlEmail(request.getRecipientEmail(), subject, htmlContent);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send test email to {}: {}", request.getRecipientEmail(), ex.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public EmailTemplateResponse resetTemplateToDefault(Long id) {
        EmailTemplate t = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + id));

        String defaultHtml = loadClasspathTemplate(t.getTemplateKey());
        if (defaultHtml == null) {
            throw new IllegalStateException("No default template file found on classpath for: " + t.getTemplateKey());
        }

        TemplateMeta meta = DEFAULT_TEMPLATES.get(t.getTemplateKey());
        if (meta != null) {
            t.setSubject(meta.defaultSubject);
            t.setDescription(meta.description);
            t.setSampleVariables(meta.sampleVariables);
        }
        t.setHtmlContent(defaultHtml);
        t.setIsActive(true);
        t.setVersion(t.getVersion() != null ? t.getVersion() + 1 : 1);
        t.setUpdatedAt(LocalDateTime.now());

        return toResponse(emailTemplateRepository.save(t));
    }

    // =========================================================================
    // NOTIFICATION RULES MANAGEMENT
    // =========================================================================

    private static final List<NotificationRuleMeta> SYSTEM_NOTIFICATION_RULES = List.of(
            new NotificationRuleMeta("notify.lead.created", "Inbound Lead Inquiries", "Leads & CRM", "When a client submits an inquiry form", "lead-created"),
            new NotificationRuleMeta("notify.lead.inquiry_ack", "Lead Auto-Acknowledgment", "Leads & CRM", "Instant confirmation reply sent to client", "lead-inquiry-acknowledgment"),
            new NotificationRuleMeta("notify.lead.converted", "Lead Converted to Customer", "Leads & CRM", "When a lead is converted into an active client account", "customer-welcome"),
            new NotificationRuleMeta("notify.invoice.created", "Invoice Issued to Client", "Billing & Finance", "When a project invoice is finalized and issued", "invoice-generated"),
            new NotificationRuleMeta("notify.invoice.paid", "Invoice Payment Received", "Billing & Finance", "When an invoice payment is verified and cleared", "invoice-paid"),
            new NotificationRuleMeta("notify.invoice.overdue", "Overdue Invoice Auto-Reminder", "Billing & Finance", "Automated scheduled reminder for approaching or overdue invoices", "invoice-reminder"),
            new NotificationRuleMeta("notify.quotation.sent", "Quotation Proposal Sent", "Billing & Finance", "Formal estimate proposal sent to client", "quotation-sent"),
            new NotificationRuleMeta("notify.project.created", "Project Initiated", "Projects & Milestones", "When a new project is created for a client", "project-status-updated"),
            new NotificationRuleMeta("notify.project.updated", "Project Milestone Update", "Projects & Milestones", "When team posts a milestone progress deliverable", "project-status-updated"),
            new NotificationRuleMeta("notify.ticket.created", "Live Support Ticket Opened", "Support & Helpdesk", "When a client opens a live support ticket", "ticket-created"),
            new NotificationRuleMeta("notify.ticket.assigned", "Support Ticket Assigned", "Support & Helpdesk", "When a ticket is assigned to a support engineer", "ticket-assigned"),
            new NotificationRuleMeta("notify.ticket.resolved", "Support Ticket Resolved", "Support & Helpdesk", "When a ticket is marked resolved or closed", "ticket-resolved"),
            new NotificationRuleMeta("notify.employee.created", "Employee Account Provisioning", "HR & Operations", "Credentials sent when staff account is created", "employee-welcome"),
            new NotificationRuleMeta("notify.payroll.generated", "Monthly Salary Slip", "HR & Operations", "Sent to employee when monthly payroll is processed", "payroll-generated"),
            new NotificationRuleMeta("notify.worklog.submitted", "Daily Employee Work Log", "HR & Operations", "When team members submit daily progress and hours", "system-notification"),
            new NotificationRuleMeta("notify.newsletter.welcome", "Newsletter Welcome", "Marketing & Growth", "Sent to subscriber upon joining mailing list", "newsletter-welcome"),
            new NotificationRuleMeta("notify.career.application", "Job Application Receipt", "Careers & Hiring", "Automated confirmation sent to job applicant", "job-application-received"),
            new NotificationRuleMeta("notify.auth.otp", "Account Security & OTP Codes", "Authentication & Security", "Verification codes and password reset tokens", "forgot-password-otp")
    );

    private static class NotificationRuleMeta {
        final String baseKey;
        final String name;
        final String category;
        final String description;
        final String templateKey;

        NotificationRuleMeta(String baseKey, String name, String category, String description, String templateKey) {
            this.baseKey = baseKey;
            this.name = name;
            this.category = category;
            this.description = description;
            this.templateKey = templateKey;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationRulesResponse getNotificationRules() {
        boolean globalEmail = getSettingBoolean("notify.global.email_enabled", true);
        boolean globalInApp = getSettingBoolean("notify.global.inapp_enabled", true);

        List<NotificationRulesResponse.NotificationRuleItem> items = new ArrayList<>();
        for (NotificationRuleMeta meta : SYSTEM_NOTIFICATION_RULES) {
            boolean emailEnabled = getSettingBoolean(meta.baseKey + ".email", true);
            boolean inAppEnabled = getSettingBoolean(meta.baseKey + ".inapp", true);

            items.add(NotificationRulesResponse.NotificationRuleItem.builder()
                    .key(meta.baseKey)
                    .name(meta.name)
                    .category(meta.category)
                    .description(meta.description)
                    .emailEnabled(emailEnabled)
                    .inAppEnabled(inAppEnabled)
                    .templateKey(meta.templateKey)
                    .build());
        }

        return NotificationRulesResponse.builder()
                .globalEmailEnabled(globalEmail)
                .globalInAppEnabled(globalInApp)
                .rules(items)
                .build();
    }

    @Override
    @Transactional
    public NotificationRulesResponse updateNotificationRules(UpdateNotificationRulesRequest request) {
        if (request.getGlobalEmailEnabled() != null) {
            saveSetting("notify.global.email_enabled", String.valueOf(request.getGlobalEmailEnabled()));
        }
        if (request.getGlobalInAppEnabled() != null) {
            saveSetting("notify.global.inapp_enabled", String.valueOf(request.getGlobalInAppEnabled()));
        }

        if (request.getRules() != null) {
            for (UpdateNotificationRulesRequest.RuleToggle r : request.getRules()) {
                if (r.getKey() != null) {
                    if (r.getEmailEnabled() != null) {
                        saveSetting(r.getKey() + ".email", String.valueOf(r.getEmailEnabled()));
                    }
                    if (r.getInAppEnabled() != null) {
                        saveSetting(r.getKey() + ".inapp", String.valueOf(r.getInAppEnabled()));
                    }
                }
            }
        }

        settingsCacheService.reloadCache();
        return getNotificationRules();
    }

    private boolean getSettingBoolean(String key, boolean defaultVal) {
        SystemSetting s = settingsCacheService.getByKey(key);
        if (s != null && s.getSettingValue() != null) {
            return "true".equalsIgnoreCase(s.getSettingValue().trim());
        }
        return defaultVal;
    }

    private void saveSetting(String key, String value) {
        Optional<SystemSetting> opt = systemSettingRepository.findBySettingKey(key);
        if (opt.isPresent()) {
            SystemSetting s = opt.get();
            s.setSettingValue(value);
            s.setUpdatedAt(LocalDateTime.now());
            systemSettingRepository.save(s);
        } else {
            SystemSetting s = SystemSetting.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .settingGroup("NOTIFICATIONS")
                    .dataType("BOOLEAN")
                    .editable(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            systemSettingRepository.save(s);
        }
    }

    private String loadClasspathTemplate(String templateName) {
        try {
            InputStream is = getClass().getResourceAsStream("/templates/emails/" + templateName + ".html");
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to load classpath email template: {}", templateName, e);
        }
        return null;
    }

    private String buildFallbackTemplateHtml(String title, String description) {
        return "<!DOCTYPE html><html><body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; color: #1e293b; padding: 24px;\">" +
                "<div style=\"max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; padding: 32px;\">" +
                "<h1 style=\"color: #0f172a; font-size: 20px; margin-top: 0;\">" + (title != null ? title : "Webliix Notification") + "</h1>" +
                "<p style=\"color: #64748b; font-size: 14px; line-height: 1.6;\">" + (description != null ? description : "") + "</p>" +
                "<div style=\"background: #f1f5f9; padding: 16px; border-radius: 6px; margin: 20px 0;\">" +
                "<p style=\"margin: 0; font-size: 14px;\">{{MESSAGE}}</p>" +
                "</div>" +
                "<p style=\"color: #94a3b8; font-size: 12px; margin-bottom: 0;\">Automated notification from Webliix Hub • https://webliix.com</p>" +
                "</div></body></html>";
    }

    private Map<String, String> getDefaultSampleVariables(String key) {
        Map<String, String> map = new HashMap<>();
        map.put("NAME", "Aarav Sharma");
        map.put("CLIENT_NAME", "Acme Technologies Pvt Ltd");
        map.put("EMAIL", "client@example.com");
        map.put("PASSWORD", "Webliix@2026!");
        map.put("LOGIN_URL", "https://login.webliix.com");
        map.put("PORTAL_URL", "https://login.webliix.com");
        map.put("OTP", "482915");
        map.put("EXPIRATION_MINUTES", "15");
        map.put("DESIGNATION", "Senior Full-Stack Engineer");
        map.put("DEPARTMENT", "Engineering & Architecture");
        map.put("INVOICE_NUMBER", "INV-2026-0042");
        map.put("AMOUNT", "₹48,500.00");
        map.put("DUE_DATE", "October 30, 2026");
        map.put("PAID_DATE", "October 11, 2026");
        map.put("PHONE", "+91 98765 43210");
        map.put("SOURCE", "Enterprise Contact Form");
        map.put("INQUIRY_DATE", "October 11, 2026");
        map.put("EMPLOYEE_NAME", "Priya Verma");
        map.put("MONTH", "October");
        map.put("YEAR", "2026");
        map.put("NET_SALARY", "₹85,000.00");
        map.put("QUOTATION_NUMBER", "QT-2026-0089");
        map.put("TOTAL_AMOUNT", "₹1,25,000.00");
        map.put("TITLE", "System Delivery Completed");
        map.put("MESSAGE", "Your requested system service tasks have been completed successfully.");
        map.put("TICKET_NUMBER", "TCK-2026-105");
        map.put("STAFF_NAME", "Rohan Mehta");
        return map;
    }

    private EmailTemplateResponse toResponse(EmailTemplate t) {
        return EmailTemplateResponse.builder()
                .id(t.getId())
                .templateKey(t.getTemplateKey())
                .subject(t.getSubject())
                .htmlContent(t.getHtmlContent())
                .plainTextContent(t.getPlainTextContent())
                .isActive(t.getIsActive())
                .description(t.getDescription())
                .sampleVariables(t.getSampleVariables())
                .version(t.getVersion())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
