package com.talentflow.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(SecurityConfigTest.AdminOnlyController.class)
@Import({SecurityConfig.class, SecurityConfigTest.AdminOnlyController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void allowsUserWithRequiredRole() {
        assertThat(mvc.get().uri("/api/v1/test/admin-only").with(tokenWithRoles("ADMIN")))
                .hasStatusOk();
    }

    @Test
    void rejectsUserWithoutRequiredRoleAsProblemDetail() {
        assertThat(mvc.get().uri("/api/v1/test/admin-only").with(tokenWithRoles("RECRUITER")))
                .hasStatus(HttpStatus.FORBIDDEN)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.status").isEqualTo(403);
    }

    @Test
    void answersCorsPreflightForConfiguredOrigin() {
        assertThat(mvc.options().uri("/api/v1/test/admin-only")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .hasStatusOk()
                .hasHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
    }

    @Test
    void rejectsCorsPreflightForUnknownOrigin() {
        assertThat(mvc.options().uri("/api/v1/test/admin-only")
                .header(HttpHeaders.ORIGIN, "https://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private static RequestPostProcessor tokenWithRoles(String... roles) {
        return jwt()
                .jwt(token -> token.claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    @RestController
    static class AdminOnlyController {

        @GetMapping("/api/v1/test/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "ok";
        }
    }
}
