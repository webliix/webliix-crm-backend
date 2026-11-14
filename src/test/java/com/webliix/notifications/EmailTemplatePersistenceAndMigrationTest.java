package com.webliix.notifications;

import com.webliix.notifications.entity.EmailTemplate;
import com.webliix.notifications.repository.EmailTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmailTemplatePersistenceAndMigrationTest {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Test
    @DisplayName("Verify EmailTemplate entity maps and persists header_html, footer_html, and sender_email columns")
    public void testEmailTemplatePersistence() {
        String testKey = "test.migration.template." + System.currentTimeMillis();
        EmailTemplate template = EmailTemplate.builder()
                .templateKey(testKey)
                .subject("Test Migration Subject")
                .htmlContent("<p>Hello {{name}}, this is content.</p>")
                .plainTextContent("Hello {{name}}, this is content.")
                .senderEmail("contact@webliix.com")
                .headerHtml("<header><div class='custom-hdr'>Logo Header</div></header>")
                .footerHtml("<footer><div class='custom-ftr'>Custom Footer</div></footer>")
                .isActive(true)
                .description("Test template to verify V68 schema columns")
                .sampleVariables("{\"name\":\"Alex\"}")
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmailTemplate saved = emailTemplateRepository.save(template);
        assertNotNull(saved.getId(), "Saved template must have a generated ID");

        Optional<EmailTemplate> retrieved = emailTemplateRepository.findByTemplateKey(testKey);
        assertTrue(retrieved.isPresent(), "Saved template must be retrievable by templateKey");

        EmailTemplate entity = retrieved.get();
        assertEquals("contact@webliix.com", entity.getSenderEmail());
        assertEquals("<header><div class='custom-hdr'>Logo Header</div></header>", entity.getHeaderHtml());
        assertEquals("<footer><div class='custom-ftr'>Custom Footer</div></footer>", entity.getFooterHtml());
        assertEquals("Test Migration Subject", entity.getSubject());
        assertEquals(true, entity.getIsActive());

        // Update header and footer
        entity.setHeaderHtml("<header>Updated Header</header>");
        entity.setFooterHtml("<footer>Updated Footer</footer>");
        entity.setSenderEmail("noreply@webliix.com");
        EmailTemplate updated = emailTemplateRepository.save(entity);

        assertEquals("<header>Updated Header</header>", updated.getHeaderHtml());
        assertEquals("<footer>Updated Footer</footer>", updated.getFooterHtml());
        assertEquals("noreply@webliix.com", updated.getSenderEmail());
    }

    @Test
    @DisplayName("Verify V68 Flyway migration script exists, defines required columns, and is safe for production")
    public void testV68MigrationScriptContent() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/migration/V68__add_email_template_header_footer_and_sender_columns.sql");
        assertTrue(resource.exists(), "V68 migration script must exist in db/migration classpath");

        String sql;
        try (InputStream is = resource.getInputStream()) {
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertNotNull(sql, "SQL content must not be null");
        assertTrue(sql.contains("ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS sender_email"),
                "Migration must add sender_email safely using IF NOT EXISTS");
        assertTrue(sql.contains("VARCHAR(120)"),
                "sender_email must match entity length 120");
        assertTrue(sql.contains("ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS header_html TEXT"),
                "Migration must add header_html safely with TEXT columnDefinition");
        assertTrue(sql.contains("ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS footer_html TEXT"),
                "Migration must add footer_html safely with TEXT columnDefinition");
        assertTrue(sql.contains("UPDATE email_templates SET sender_email = 'noreply@webliix.com' WHERE sender_email IS NULL"),
                "Migration must backfill null sender_email values safely for existing rows");
    }
}
