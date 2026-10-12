package com.talentflow.pipeline.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.pipeline.application.PipelineService;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(PipelineController.class)
@Import(SecurityConfig.class)
class PipelineControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PipelineService pipeline;

    @Test
    void recruiterMovesStage() {
        when(pipeline.moveStage(eq(ID), eq(ApplicationStage.INTERVIEW), any())).thenReturn(StageChange.record(ID,
                ApplicationStage.SCREENING, ApplicationStage.INTERVIEW, "kc", "Anna", null, Instant.now()));

        var response = mvc.patch().uri("/api/v1/applications/{id}/stage", ID).with(as("RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"stage\": \"INTERVIEW\"}");

        assertThat(response).hasStatusOk().bodyJson().extractingPath("$.toStage").isEqualTo("INTERVIEW");
    }

    @Test
    void hiringManagerCannotMoveStages() {
        var response = mvc.patch().uri("/api/v1/applications/{id}/stage", ID).with(as("HIRING_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"stage\": \"INTERVIEW\"}");

        assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
        verify(pipeline, never()).moveStage(any(), any(), any());
    }

    @Test
    void reportsInvalidTransitionAsUnprocessable() {
        when(pipeline.moveStage(any(), any(), any())).thenThrow(new BusinessRuleException("Only candidates with an offer can be marked as hired."));

        var response = mvc.patch().uri("/api/v1/applications/{id}/stage", ID).with(as("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"stage\": \"HIRED\"}");

        assertThat(response).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void requiresAStage() {
        var response = mvc.patch().uri("/api/v1/applications/{id}/stage", ID).with(as("RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"note\": \"hi\"}");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void candidateCannotSeeTheBoard() {
        assertThat(mvc.get().uri("/api/v1/jobs/{id}/applications", ID).with(as("CANDIDATE")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
