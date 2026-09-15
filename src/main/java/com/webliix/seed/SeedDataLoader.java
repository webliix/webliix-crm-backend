package com.webliix.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.webliix.billing.entity.Plan;
import com.webliix.billing.entity.Subscription;
import com.webliix.billing.repository.PlanRepository;
import com.webliix.billing.repository.SubscriptionRepository;
import com.webliix.notifications.entity.EmailTemplate;
import com.webliix.notifications.repository.EmailTemplateRepository;
import com.webliix.tenant.entity.Tenant;
import com.webliix.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataLoader implements CommandLineRunner {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;
    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Webliix seed data initialization...");

        seedPlans();
        seedEmailTemplates();
        seedDemoTenant();

        log.info("Seed data initialization completed.");
    }

    private void seedPlans() {
        if (planRepository.count() > 0) {
            log.info("Plans already exist. Skipping...");
            return;
        }

        log.info("Creating billing plans...");

        Plan starter = Plan.builder()
                .name("Starter")
                .description("Perfect for small businesses")
                .pricePerMonth(new BigDecimal("29.00"))
                .pricePerYear(new BigDecimal("290.00"))
                .maxUsers(5)
                .maxStorageGb(10)
                .maxApiCallsPerMonth(10000L)
                .features("[\"CRM\", \"Basic Projects\", \"Email Support\"]")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Plan business = Plan.builder()
                .name("Business")
                .description("Ideal for growing teams")
                .pricePerMonth(new BigDecimal("99.00"))
                .pricePerYear(new BigDecimal("990.00"))
                .maxUsers(25)
                .maxStorageGb(100)
                .maxApiCallsPerMonth(100000L)
                .features("[\"CRM\", \"Projects\", \"Finance\", \"HR\", \"Tickets\", \"Priority Support\"]")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Plan enterprise = Plan.builder()
                .name("Enterprise")
                .description("For large organizations")
                .pricePerMonth(new BigDecimal("299.00"))
                .pricePerYear(new BigDecimal("2990.00"))
                .maxUsers(999)
                .maxStorageGb(1000)
                .maxApiCallsPerMonth(1000000L)
                .features("[\"All Features\", \"Custom Integrations\", \"Dedicated Support\", \"SLA\"]")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        planRepository.saveAll(Arrays.asList(starter, business, enterprise));
        log.info("Created 3 plans: Starter, Business, Enterprise");
    }

    private void seedEmailTemplates() {
        if (emailTemplateRepository.count() > 0) {
            log.info("Email templates already exist. Skipping...");
            return;
        }

        log.info("Creating email templates...");

        EmailTemplate welcome = EmailTemplate.builder()
                .templateKey("WELCOME_EMAIL")
                .subject("Welcome to Webliix!")
                .htmlContent("<p>Hello {name},</p><p>Welcome to Webliix! Your account has been created.</p>")
                .plainTextContent("Hello {name}, Welcome to Webliix!")
                .isActive(true)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmailTemplate passwordReset = EmailTemplate.builder()
                .templateKey("PASSWORD_RESET")
                .subject("Reset your Webliix password")
                .htmlContent("<p>Click <a href='{reset_link}'>here</a> to reset your password.</p>")
                .plainTextContent("Click the link to reset your password: {reset_link}")
                .isActive(true)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmailTemplate invoiceSent = EmailTemplate.builder()
                .templateKey("INVOICE_SENT")
                .subject("Invoice {invoice_number} from {company_name}")
                .htmlContent("<p>Hi {customer_name},</p><p>Invoice {invoice_number} has been sent. Total: {total}</p>")
                .plainTextContent("Invoice {invoice_number} sent. Total: {total}")
                .isActive(true)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmailTemplate paymentReceived = EmailTemplate.builder()
                .templateKey("PAYMENT_RECEIVED")
                .subject("Payment received for {invoice_number}")
                .htmlContent("<p>Payment of {amount} has been received. Thank you!</p>")
                .plainTextContent("Payment received: {amount}")
                .isActive(true)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        emailTemplateRepository.saveAll(Arrays.asList(welcome, passwordReset, invoiceSent, paymentReceived));
        log.info("Created 4 email templates");
    }

    private void seedDemoTenant() {
        if (tenantRepository.findByName("Webliix Demo").isPresent()) {
            log.info("Demo tenant already exists. Skipping...");
            return;
        }

        log.info("Creating demo tenant...");
        Tenant demoTenant = Tenant.builder()
                .name("Webliix Demo")
                .description("Demo tenant for testing all features")
                .subdomain("demo")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tenantRepository.save(demoTenant);
        log.info("Created demo tenant: Webliix Demo");

        // Create subscription to Business plan for demo tenant
        Plan businessPlan = planRepository.findByName("Business")
                .orElseThrow(() -> new RuntimeException("Business plan not found"));

        Subscription demoSubscription = Subscription.builder()
                .tenantId(demoTenant.getId())
                .planId(businessPlan.getId())
                .status("ACTIVE")
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(10))
                .nextBillingDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(demoSubscription);
        log.info("Created subscription for demo tenant to Business plan");
    }
}
