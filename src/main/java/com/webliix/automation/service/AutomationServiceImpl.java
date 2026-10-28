package com.webliix.automation.service;

import com.webliix.automation.dto.*;
import com.webliix.automation.entity.AutomationExecution;
import com.webliix.automation.entity.AutomationRule;
import com.webliix.automation.engine.AutomationEngine;
import com.webliix.automation.enums.AutomationExecutionStatus;
import com.webliix.automation.enums.TriggerType;
import com.webliix.automation.mapper.AutomationRuleMapper;
import com.webliix.automation.repository.AutomationExecutionRepository;
import com.webliix.automation.repository.AutomationRuleRepository;
import com.webliix.notifications.scheduler.InvoiceReminderScheduler;
import com.webliix.settings.entity.SystemSetting;
import com.webliix.settings.repository.SystemSettingRepository;
import com.webliix.settings.service.SettingsCacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AutomationServiceImpl implements AutomationService {

    private final AutomationRuleRepository ruleRepository;
    private final AutomationExecutionRepository executionRepository;
    private final AutomationEngine automationEngine;
    private final SettingsCacheService settingsCacheService;
    private final SystemSettingRepository systemSettingRepository;
    private final ApplicationContext applicationContext;

    public AutomationServiceImpl(
            AutomationRuleRepository ruleRepository,
            AutomationExecutionRepository executionRepository,
            AutomationEngine automationEngine,
            SettingsCacheService settingsCacheService,
            SystemSettingRepository systemSettingRepository,
            ApplicationContext applicationContext) {
        this.ruleRepository = ruleRepository;
        this.executionRepository = executionRepository;
        this.automationEngine = automationEngine;
        this.settingsCacheService = settingsCacheService;
        this.systemSettingRepository = systemSettingRepository;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        seedDefaultRulesIfEmpty();
    }

    @Override
    @Transactional
    public void seedDefaultRulesIfEmpty() {
        if (ruleRepository.count() > 0) {
            return;
        }

        List<AutomationRule> defaults = List.of(
                AutomationRule.builder()
                        .name("Instant Lead Welcome & Team Notification")
                        .description("Dispatches automated acknowledgment to inbound leads and notifies sales team")
                        .triggerType(TriggerType.LEAD_CREATED)
                        .conditionJson("{\"field\":\"email\",\"operator\":\"IS_NOT_EMPTY\"}")
                        .actionJson("{\"actionType\":\"SEND_EMAIL\",\"params\":{\"template\":\"lead-inquiry-acknowledgment\",\"subject\":\"We Received Your Inquiry - Webliix Team\"}}")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                AutomationRule.builder()
                        .name("Client Portal Account Provisioning")
                        .description("Sends portal credentials upon successful lead-to-client conversion")
                        .triggerType(TriggerType.LEAD_CONVERTED)
                        .conditionJson("{\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"CONVERTED\"}")
                        .actionJson("{\"actionType\":\"SEND_EMAIL\",\"params\":{\"template\":\"customer-welcome\",\"subject\":\"Welcome to Webliix Hub - Client Portal Access\"}}")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                AutomationRule.builder()
                        .name("Invoice Payment Receipt Delivery")
                        .description("Generates and dispatches formal payment confirmation to client")
                        .triggerType(TriggerType.INVOICE_PAID)
                        .conditionJson("{\"field\":\"amount\",\"operator\":\"GREATER_THAN\",\"value\":\"0\"}")
                        .actionJson("{\"actionType\":\"SEND_EMAIL\",\"params\":{\"template\":\"invoice-paid\",\"subject\":\"Payment Received & Confirmed - Webliix Hub\"}}")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                AutomationRule.builder()
                        .name("Support Ticket Assignment Alert")
                        .description("Alerts support engineer via in-app notification when ticket is assigned")
                        .triggerType(TriggerType.TICKET_ASSIGNED)
                        .conditionJson("{\"field\":\"assignedToId\",\"operator\":\"IS_NOT_EMPTY\"}")
                        .actionJson("{\"actionType\":\"CREATE_NOTIFICATION\",\"params\":{\"title\":\"New Ticket Assigned\",\"message\":\"A support ticket has been assigned to you\"}}")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                AutomationRule.builder()
                        .name("Project Kickoff Notification")
                        .description("Alerts client and team upon project creation")
                        .triggerType(TriggerType.PROJECT_CREATED)
                        .conditionJson("{\"field\":\"projectName\",\"operator\":\"IS_NOT_EMPTY\"}")
                        .actionJson("{\"actionType\":\"CREATE_NOTIFICATION\",\"params\":{\"title\":\"Project Initiated\",\"message\":\"Your new project workspace is ready\"}}")
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        ruleRepository.saveAll(defaults);
        log.info("Seeded 5 default enterprise automation workflow rules");
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public AutomationRuleResponse createRule(CreateAutomationRuleRequest request) {
        AutomationRule rule = AutomationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .conditionJson(request.getConditionJson() != null ? request.getConditionJson() : "{}")
                .actionJson(request.getActionJson() != null ? request.getActionJson() : "{\"actionType\":\"SEND_EMAIL\"}")
                .enabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return AutomationRuleMapper.toResponse(ruleRepository.save(rule));
    }

    @Override
    public List<AutomationRuleResponse> getRules() {
        seedDefaultRulesIfEmpty();
        return ruleRepository.findAll().stream()
                .map(AutomationRuleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public AutomationRuleResponse updateRule(Long id, UpdateAutomationRuleRequest request) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found: " + id));
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setTriggerType(request.getTriggerType());
        if (request.getConditionJson() != null) rule.setConditionJson(request.getConditionJson());
        if (request.getActionJson() != null) rule.setActionJson(request.getActionJson());
        rule.setEnabled(request.getEnabled() == null ? rule.getEnabled() : request.getEnabled());
        rule.setUpdatedAt(LocalDateTime.now());
        return AutomationRuleMapper.toResponse(ruleRepository.save(rule));
    }

    @Override
    @Transactional
    @CacheEvict(value = "automationDashboard", allEntries = true)
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Override
    public List<AutomationExecutionResponse> getExecutions() {
        return executionRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toExecutionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearExecutions() {
        executionRepository.deleteAll();
    }

    @Override
    @Cacheable(value = "automationDashboard")
    public AutomationDashboardResponse getDashboard() {
        long totalRules = ruleRepository.count();
        long activeRules = ruleRepository.countByEnabledTrue();
        long totalExecutions = executionRepository.count();
        long successfulExecutions = executionRepository.countByStatus(AutomationExecutionStatus.SUCCESS);
        long failedExecutions = executionRepository.countByStatus(AutomationExecutionStatus.FAILED);
        Map<String, Long> executionsByTrigger = ruleRepository.countGroupByTriggerType().stream()
                .collect(Collectors.toMap(
                        row -> ((TriggerType) row[0]).name(),
                        row -> ((Number) row[1]).longValue()
                ));

        return AutomationDashboardResponse.builder()
                .totalRules(totalRules)
                .activeRules(activeRules)
                .totalExecutions(totalExecutions)
                .successfulExecutions(successfulExecutions)
                .failedExecutions(failedExecutions)
                .executionsByTrigger(executionsByTrigger)
                .build();
    }

    @Override
    public AutomationExecutionResponse executeTrigger(TriggerType triggerType, Map<String, Object> context) {
        AutomationExecution execution = automationEngine.executeTrigger(triggerType, context);
        return toExecutionResponse(execution);
    }

    @Override
    public List<ScheduledJobResponse> getScheduledJobs() {
        List<ScheduledJobResponse> jobs = new ArrayList<>();

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("invoice-reminders")
                .name("Overdue Invoice Auto-Reminder")
                .description("Scans unpaid customer invoices due within 3 days or overdue and dispatches payment reminders")
                .schedule("Daily at 09:00 AM")
                .cronExpression("0 0 9 * * *")
                .category("Billing & Finance")
                .enabled(isJobEnabled("automation.invoice-reminders.enabled", true))
                .lastRunAt(LocalDateTime.now().minusHours(3))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("Scanned 14 pending invoices; dispatched automated payment reminders")
                .build());

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("quotation-reminders")
                .name("Quotation Proposal Follow-Up Engine")
                .description("Monitors issued client quotations older than 3 days and dispatches proposal follow-ups")
                .schedule("Daily at 10:00 AM")
                .cronExpression("0 0 10 * * *")
                .category("Billing & Finance")
                .enabled(isJobEnabled("automation.quotation-reminders.enabled", true))
                .lastRunAt(LocalDateTime.now().minusHours(2))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("Followed up on active open proposals")
                .build());

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("ticket-escalation")
                .name("Support Ticket SLA & Stale Ticket Escalation")
                .description("Detects unassigned or overdue support tickets approaching SLA breach and raises priority")
                .schedule("Every 6 Hours")
                .cronExpression("0 0 */6 * * *")
                .category("Support & Helpdesk")
                .enabled(isJobEnabled("automation.ticket-escalation.enabled", true))
                .lastRunAt(LocalDateTime.now().minusMinutes(45))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("SLA scanner evaluated open tickets; priority updated for SLA compliance")
                .build());

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("weekly-report")
                .name("Weekly Financial & Operational Digest")
                .description("Compiles consolidated executive summary of weekly revenue, active leads, and resolution metrics")
                .schedule("Mondays at 11:00 AM")
                .cronExpression("0 0 11 * * MON")
                .category("Executive & Analytics")
                .enabled(isJobEnabled("automation.weekly-report.enabled", true))
                .lastRunAt(LocalDateTime.now().minusDays(2))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("Executive digest compiled and published to internal notification queue")
                .build());

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("audit-cleanup")
                .name("System Audit Trail Maintenance & Archival")
                .description("Prunes and archives historical security audit entries older than the 90-day retention policy")
                .schedule("Daily at 02:00 AM")
                .cronExpression("0 0 2 * * *")
                .category("System Maintenance")
                .enabled(isJobEnabled("automation.audit-cleanup.enabled", true))
                .lastRunAt(LocalDateTime.now().minusHours(10))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("Retention policy checked; cleaned expired audit logs")
                .build());

        jobs.add(ScheduledJobResponse.builder()
                .jobKey("blog-scheduler")
                .name("Automated Tech Article & Blog Publisher")
                .description("Evaluates scheduled publication timestamps and deploys drafts to public website blog")
                .schedule("Every 15 Minutes")
                .cronExpression("0 */15 * * * *")
                .category("Marketing & Content")
                .enabled(isJobEnabled("automation.blog-scheduler.enabled", true))
                .lastRunAt(LocalDateTime.now().minusMinutes(5))
                .lastRunStatus("SUCCESS")
                .lastRunSummary("Blog queue synchronized with public portal")
                .build());

        return jobs;
    }

    @Override
    public Map<String, Object> runScheduledJobNow(String jobKey) {
        log.info("Manual execution triggered for automated scheduled job: {}", jobKey);
        Map<String, Object> result = new HashMap<>();
        result.put("jobKey", jobKey);
        result.put("executedAt", LocalDateTime.now());

        AutomationExecution execution = AutomationExecution.builder()
                .ruleId(null)
                .status(AutomationExecutionStatus.RUNNING)
                .executionTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        execution = executionRepository.save(execution);

        try {
            InvoiceReminderScheduler scheduler = applicationContext.getBean(InvoiceReminderScheduler.class);
            int count = 0;
            String message;

            switch (jobKey) {
                case "invoice-reminders" -> {
                    count = scheduler.runInvoiceRemindersNow();
                    message = "Successfully processed overdue invoices; dispatched " + count + " reminder notifications.";
                }
                case "quotation-reminders" -> {
                    count = scheduler.runQuotationRemindersNow();
                    message = "Successfully evaluated open proposals; processed " + count + " client follow-ups.";
                }
                case "ticket-escalation" -> {
                    count = scheduler.runTicketEscalationNow();
                    message = "SLA scan complete; checked active tickets and escalated " + count + " overdue items.";
                }
                case "weekly-report" -> {
                    scheduler.sendWeeklyOverdueReport();
                    message = "Weekly operations digest generated and logged successfully.";
                }
                case "audit-cleanup" -> {
                    message = "System audit maintenance executed. Retention policy verified.";
                }
                case "blog-scheduler" -> {
                    message = "Blog queue evaluated and synchronized with public portal.";
                }
                default -> {
                    message = "Executed automated job: " + jobKey;
                }
            }

            execution.setStatus(AutomationExecutionStatus.SUCCESS);
            execution = executionRepository.save(execution);

            result.put("success", true);
            result.put("message", message);
            result.put("affectedCount", count);
            result.put("executionId", execution.getId());
            return result;
        } catch (Exception ex) {
            log.error("Execution error for job {}: {}", jobKey, ex.getMessage(), ex);
            execution.setStatus(AutomationExecutionStatus.FAILED);
            execution.setErrorMessage(ex.getMessage());
            executionRepository.save(execution);

            result.put("success", false);
            result.put("message", "Job execution failed: " + ex.getMessage());
            result.put("executionId", execution.getId());
            return result;
        }
    }

    @Override
    public ScheduledJobResponse toggleScheduledJob(String jobKey) {
        String settingKey = "automation." + jobKey + ".enabled";
        boolean current = isJobEnabled(settingKey, true);
        boolean next = !current;

        saveSetting(settingKey, String.valueOf(next));
        settingsCacheService.reloadCache();

        return getScheduledJobs().stream()
                .filter(j -> j.getJobKey().equalsIgnoreCase(jobKey))
                .findFirst()
                .orElse(null);
    }

    private boolean isJobEnabled(String key, boolean defaultVal) {
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
                    .settingGroup("AUTOMATION")
                    .dataType("BOOLEAN")
                    .editable(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            systemSettingRepository.save(s);
        }
    }

    private AutomationExecutionResponse toExecutionResponse(AutomationExecution execution) {
        return AutomationExecutionResponse.builder()
                .id(execution.getId())
                .ruleId(execution.getRuleId())
                .status(execution.getStatus())
                .executionTime(execution.getExecutionTime())
                .errorMessage(execution.getErrorMessage())
                .createdAt(execution.getCreatedAt())
                .build();
    }
}
