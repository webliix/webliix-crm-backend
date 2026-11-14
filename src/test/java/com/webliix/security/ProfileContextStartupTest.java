package com.webliix.security;

import com.webliix.WebliixApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileContextStartupTest {

    @BeforeEach
    public void setup() {
        System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        System.setProperty("spring.jpa.properties.hibernate.hbm2ddl.auto", "create-drop");
        System.setProperty("spring.flyway.enabled", "false");
        System.setProperty("FLYWAY_ENABLED", "false");
    }

    @AfterEach
    public void teardown() {
        System.clearProperty("spring.jpa.hibernate.ddl-auto");
        System.clearProperty("spring.jpa.properties.hibernate.hbm2ddl.auto");
        System.clearProperty("spring.flyway.enabled");
        System.clearProperty("FLYWAY_ENABLED");
    }

    @Test
    @DisplayName("Verify Spring Boot context starts successfully with 'local' profile using environment variables")
    public void testLocalProfileStartup() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(WebliixApplication.class)
                .profiles("local")
                .properties(
                        "server.port=0",
                        "PORT=0",
                        "DB_URL=jdbc:h2:mem:local_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.url=jdbc:h2:mem:local_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "DB_USERNAME=sa",
                        "DB_PASSWORD=",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "spring.cache.type=none",
                        "storage.provider=local",
                        "storage.upload-dir=target/test-uploads"
                )
                .run()) {

            assertNotNull(context);
            assertTrue(context.isActive());
            assertTrue(context.containsBean("requirePermissionAspect"),
                    "Authoritative requirePermissionAspect bean must be registered in local profile");
        }
    }

    @Test
    @DisplayName("Verify Spring Boot context starts successfully with 'production' profile using environment variables")
    public void testProductionProfileStartup() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(WebliixApplication.class)
                .profiles("production")
                .properties(
                        "server.port=0",
                        "PORT=0",
                        "DB_URL=jdbc:h2:mem:prod_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.url=jdbc:h2:mem:prod_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "DB_USERNAME=sa",
                        "DB_PASSWORD=",
                        "spring.datasource.username=sa",
                        "spring.datasource.password=",
                        "JWT_SECRET=super-secure-production-jwt-secret-key-32-bytes-minimum!",
                        "MAIL_USERNAME=apikey",
                        "MAIL_PASSWORD=test-mail-password",
                        "spring.cache.type=none",
                        "storage.provider=local",
                        "storage.upload-dir=target/test-uploads"
                )
                .run()) {

            assertNotNull(context);
            assertTrue(context.isActive());
            assertTrue(context.containsBean("requirePermissionAspect"),
                    "Authoritative requirePermissionAspect bean must be registered in production profile");
        }
    }
}
