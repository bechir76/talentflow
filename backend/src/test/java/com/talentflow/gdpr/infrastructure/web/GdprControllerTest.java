package com.talentflow.gdpr.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.talentflow.gdpr.application.CandidateDataExport;
import com.talentflow.gdpr.application.MyDataService;
import com.talentflow.gdpr.application.RetentionService;
import com.talentflow.gdpr.domain.RetentionPolicy;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(GdprController.class)
@Import(SecurityConfig.class)
class GdprControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private MyDataService myData;
    @MockitoBean
    private RetentionService retention;

    @Test
    void candidateDownloadsDataExportAsAttachment() {
        when(myData.export()).thenReturn(new CandidateDataExport(Instant.now(),
                new CandidateDataExport.Profile(UUID.randomUUID(), "Clara", "c@x", null, null, Instant.now()), List.of(), List.of()));

        assertThat(mvc.get().uri("/api/v1/me/data-export").with(as("CANDIDATE")))
                .hasStatusOk()
                .hasHeader("Content-Disposition", "attachment; filename=\"talentflow-data-export.json\"")
                .bodyJson().extractingPath("$.profile.fullName").isEqualTo("Clara");
    }

    @Test
    void candidateDeletesOwnData() {
        assertThat(mvc.delete().uri("/api/v1/me").with(as("CANDIDATE"))).hasStatus(HttpStatus.NO_CONTENT);
        verify(myData).deleteMyData();
    }

    @Test
    void onlyAdminsManageRetention() {
        assertThat(mvc.get().uri("/api/v1/admin/retention-policy").with(as("RECRUITER"))).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(mvc.delete().uri("/api/v1/me").with(as("RECRUITER"))).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void validatesRetentionPeriod() {
        assertThat(mvc.put().uri("/api/v1/admin/retention-policy").with(as("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"rejectedRetentionMonths\": 0, \"autoDeleteEnabled\": true}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void adminUpdatesRetention() {
        when(retention.updatePolicy(12, true)).thenReturn(new RetentionPolicy(UUID.randomUUID(), 12, true, Instant.now()));

        assertThat(mvc.put().uri("/api/v1/admin/retention-policy").with(as("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"rejectedRetentionMonths\": 12, \"autoDeleteEnabled\": true}"))
                .hasStatusOk().bodyJson().extractingPath("$.rejectedRetentionMonths").isEqualTo(12);
    }

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
