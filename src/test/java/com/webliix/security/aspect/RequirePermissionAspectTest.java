package com.webliix.security.aspect;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.entity.ScopeType;
import com.webliix.security.evaluator.PermissionEvaluator;
import com.webliix.security.model.WebliixPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RequirePermissionAspectTest {

    // Target test class demonstrating both class-level and method-level @RequirePermission
    @RequirePermission("CRM_VIEW")
    public static class SampleSecuredService {

        public String viewOnly() {
            return "view-data";
        }

        @RequirePermission("CRM_DELETE")
        public String deleteItem() {
            return "deleted";
        }

        @RequirePermission(anyOf = {"JOBS_VIEW", "JOBS_MANAGE"})
        public String viewJobs() {
            return "jobs-data";
        }

        @RequirePermission
        public String authenticatedOnly() {
            return "auth-data";
        }
    }

    private SampleSecuredService proxyService;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    public void setup() {
        PermissionEvaluator evaluator = new PermissionEvaluator();
        RequirePermissionAspect aspect = new RequirePermissionAspect(evaluator);

        SampleSecuredService target = new SampleSecuredService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        proxyService = factory.getProxy();

        mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void authenticateUser(String role, Set<String> permissions, boolean isSuperAdmin) {
        WebliixPrincipal principal = WebliixPrincipal.builder()
                .userId(1L)
                .username("test@webliix.com")
                .roles(role != null ? Set.of(role) : Set.of())
                .permissions(permissions.stream().collect(
                        java.util.stream.Collectors.toMap(p -> p, p -> ScopeType.ALL)
                ))
                .superAdmin(isSuperAdmin)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "N/A", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Permitted user with required permission succeeds")
    public void testPermittedUserAccess() {
        authenticateUser("EMPLOYEE", Set.of("CRM_VIEW"), false);

        String result = proxyService.viewOnly();
        assertEquals("view-data", result);
    }

    @Test
    @DisplayName("Unauthorized user lacking permission is denied with AccessDeniedException and REQUIRED_PERMISSION attribute set")
    public void testUnauthorizedUserDenied() {
        authenticateUser("EMPLOYEE", Set.of("SOME_OTHER_PERMISSION"), false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> proxyService.viewOnly());
        assertTrue(ex.getMessage().contains("Required permission: CRM_VIEW"));
        assertEquals("CRM_VIEW", mockRequest.getAttribute("REQUIRED_PERMISSION"));
    }

    @Test
    @DisplayName("Method-level annotation overrides class-level: user with only class permission is denied for stricter method")
    public void testMethodLevelOverridesClassLevel() {
        authenticateUser("EMPLOYEE", Set.of("CRM_VIEW"), false);

        // Can access class-level method
        assertEquals("view-data", proxyService.viewOnly());

        // Denied on method requiring CRM_DELETE
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> proxyService.deleteItem());
        assertTrue(ex.getMessage().contains("Required permission: CRM_DELETE"));
        assertEquals("CRM_DELETE", mockRequest.getAttribute("REQUIRED_PERMISSION"));

        // When granted CRM_DELETE, succeeds
        authenticateUser("EMPLOYEE", Set.of("CRM_VIEW", "CRM_DELETE"), false);
        assertEquals("deleted", proxyService.deleteItem());
    }

    @Test
    @DisplayName("anyOf permission evaluation: user with any of the allowed permissions succeeds")
    public void testAnyOfPermissionEvaluation() {
        // User with JOBS_VIEW succeeds
        authenticateUser("EMPLOYEE", Set.of("JOBS_VIEW"), false);
        assertEquals("jobs-data", proxyService.viewJobs());

        // User with JOBS_MANAGE succeeds
        authenticateUser("EMPLOYEE", Set.of("JOBS_MANAGE"), false);
        assertEquals("jobs-data", proxyService.viewJobs());

        // User lacking both is denied
        authenticateUser("EMPLOYEE", Set.of("OTHER_PERM"), false);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> proxyService.viewJobs());
        assertTrue(ex.getMessage().contains("Required permission: JOBS_VIEW, JOBS_MANAGE"));
        assertEquals("JOBS_VIEW, JOBS_MANAGE", mockRequest.getAttribute("REQUIRED_PERMISSION"));
    }

    @Test
    @DisplayName("Super admin and Admin roles bypass all RequirePermission checks")
    public void testSuperAdminBypassesAllChecks() {
        authenticateUser("SUPER_ADMIN", Set.of(), true);

        assertEquals("view-data", proxyService.viewOnly());
        assertEquals("deleted", proxyService.deleteItem());
        assertEquals("jobs-data", proxyService.viewJobs());
        assertEquals("auth-data", proxyService.authenticatedOnly());
    }

    @Test
    @DisplayName("Unauthenticated caller is denied on protected methods")
    public void testUnauthenticatedCallerDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> proxyService.viewOnly());
        assertThrows(AccessDeniedException.class, () -> proxyService.deleteItem());
    }
}
