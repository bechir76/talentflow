package com.talentflow.user.infrastructure.web;

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
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.talentflow.shared.infrastructure.security.JwtCurrentUserProvider;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(MeController.class)
@Import({SecurityConfig.class, JwtCurrentUserProvider.class})
class MeControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsCurrentUserFromToken() {
        var response = mvc.get().uri("/api/v1/me")
                .with(jwt()
                        .jwt(token -> token.subject("kc-user-42")
                                .claim("email", "recruiter@talentflow.dev")
                                .claim("name", "Anna Keller")
                                .claim("realm_access", Map.of("roles", List.of("RECRUITER", "offline_access"))))
                        .authorities(new KeycloakRealmRoleConverter()));

        assertThat(response).hasStatusOk().bodyJson().isLenientlyEqualTo("""
                {
                  "id": "kc-user-42",
                  "email": "recruiter@talentflow.dev",
                  "fullName": "Anna Keller",
                  "roles": ["RECRUITER"]
                }
                """);
    }

    @Test
    void rejectsRequestWithoutTokenAsProblemDetail() {
        var response = mvc.get().uri("/api/v1/me");

        assertThat(response)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .containsHeader(HttpHeaders.WWW_AUTHENTICATE)
                .bodyJson()
                .extractingPath("$.status").isEqualTo(401);
    }
}
