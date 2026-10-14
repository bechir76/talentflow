package com.talentflow.gdpr.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.talentflow.gdpr.application.RetentionService;
import com.talentflow.gdpr.domain.RetentionPolicy;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(PrivacyPolicyController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "talentflow.privacy.policy-version=2026-10")
class PrivacyPolicyControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RetentionService retention;

    @Test
    void showsPolicyVersionAndRetentionWithoutAuthentication() {
        when(retention.policy()).thenReturn(new RetentionPolicy(UUID.randomUUID(), 6, true, Instant.now()));

        var response = mvc.get().uri("/api/v1/public/privacy-policy");

        assertThat(response).hasStatusOk().bodyJson().isEqualTo("""
                {"version":"2026-10","rejectedRetentionMonths":6,"autoDeleteEnabled":true}""");
    }
}
