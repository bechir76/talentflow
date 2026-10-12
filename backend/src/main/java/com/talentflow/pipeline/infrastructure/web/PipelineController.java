package com.talentflow.pipeline.infrastructure.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.application.domain.AiStatus;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.pipeline.application.PipelineService;
import com.talentflow.pipeline.application.PipelineService.PipelineCard;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.shared.infrastructure.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Pipeline")
public class PipelineController {

    private static final int MAX_CARD_SKILLS = 3;

    private final PipelineService pipeline;

    public PipelineController(PipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @GetMapping("/api/v1/jobs/{jobId}/applications")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Applications of a job for the Kanban board")
    public PageResponse<PipelineCardResponse> board(@PathVariable UUID jobId,
            @PageableDefault(size = 100, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(pipeline.board(jobId, pageable), PipelineCardResponse::from);
    }

    @PatchMapping("/api/v1/applications/{id}/stage")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Moves an application to another stage; the candidate is notified by email")
    public StageChangeResponse moveStage(@PathVariable UUID id, @Valid @RequestBody StageChangeRequest request) {
        return StageChangeResponse.from(pipeline.moveStage(id, request.stage(), request.note()));
    }

    @GetMapping("/api/v1/applications/{id}/stage-changes")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Stage history of an application, newest first")
    public List<StageChangeResponse> history(@PathVariable UUID id) {
        return pipeline.history(id).stream().map(StageChangeResponse::from).toList();
    }

    public record StageChangeRequest(@NotNull ApplicationStage stage, @Size(max = StageChange.MAX_NOTE_LENGTH) String note) {
    }

    public record StageChangeResponse(UUID id, ApplicationStage fromStage, ApplicationStage toStage,
            String changedByName, Instant changedAt, String note) {

        static StageChangeResponse from(StageChange change) {
            return new StageChangeResponse(change.getId(), change.getFromStage(), change.getToStage(),
                    change.getChangedByName(), change.getChangedAt(), change.getNote());
        }
    }

    public record PipelineCardResponse(UUID id, String candidateName, String candidateLocation, ApplicationStage stage,
            Instant stageChangedAt, Instant appliedAt, AiStatus aiStatus, Integer aiScore, List<String> topSkills) {

        static PipelineCardResponse from(PipelineCard card) {
            JobApplication application = card.application();
            List<String> skills = application.getAiMatchingSkills();
            return new PipelineCardResponse(application.getId(),
                    card.candidate() == null ? null : card.candidate().getFullName(),
                    card.candidate() == null ? null : card.candidate().getLocation(),
                    application.getStage(), application.getStageChangedAt(), application.getAppliedAt(),
                    application.getAiStatus(), application.getAiScore(),
                    skills.subList(0, Math.min(MAX_CARD_SKILLS, skills.size())));
        }
    }
}
