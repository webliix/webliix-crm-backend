package com.webliix.security;

import com.webliix.security.annotation.RequirePermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultDenyTest {

    // Allowlist of controllers that are intentionally open/public without RBAC requirements
    private static final Set<String> PUBLIC_CONTROLLER_SIMPLE_NAMES = Set.of(
            "PublicBlogController",
            "PublicLeadController",
            "PublicNewsletterController",
            "PublicReviewController",
            "PublicTicketController",
            "PublicPingController",
            "PortalAuthController"
    );

    // Allowlist of individual public methods in controllers (e.g. AuthController login/register)
    private static final Set<String> PUBLIC_METHOD_NAMES = Set.of(
            "register",
            "login",
            "forgotPassword",
            "verifyResetOtp",
            "resetPassword",
            "verifyEmail",
            "resendVerificationOtp"
    );

    @Test
    @DisplayName("Default-Deny Audit: Every CRM endpoint must declare @RequirePermission or be explicitly allowlisted")
    public void allCrmEndpointsMustHaveRequirePermission() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> violations = new ArrayList<>();
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.webliix");

        for (BeanDefinition bd : candidates) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            // Skip public controllers
            if (PUBLIC_CONTROLLER_SIMPLE_NAMES.contains(clazz.getSimpleName())) {
                continue;
            }

            boolean classHasPermission = clazz.isAnnotationPresent(RequirePermission.class);

            for (Method method : clazz.getDeclaredMethods()) {
                boolean isEndpoint = method.isAnnotationPresent(RequestMapping.class)
                        || method.isAnnotationPresent(GetMapping.class)
                        || method.isAnnotationPresent(PostMapping.class)
                        || method.isAnnotationPresent(PutMapping.class)
                        || method.isAnnotationPresent(DeleteMapping.class)
                        || method.isAnnotationPresent(PatchMapping.class);

                if (!isEndpoint) {
                    continue;
                }

                // Skip allowlisted public methods in AuthController
                if (clazz.getSimpleName().equals("AuthController") && PUBLIC_METHOD_NAMES.contains(method.getName())) {
                    continue;
                }

                boolean methodHasPermission = method.isAnnotationPresent(RequirePermission.class);

                if (!classHasPermission && !methodHasPermission) {
                    violations.add(String.format("Unsecured Endpoint: %s#%s() lacks @RequirePermission",
                            clazz.getName(), method.getName()));
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Default-Deny Security Violation! Found " + violations.size()
                        + " endpoint(s) without @RequirePermission:\n"
                        + String.join("\n", violations));
    }
}
