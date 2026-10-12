package com.talentflow.application.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.application.application.MyApplicationsService;
import com.talentflow.application.application.MyApplicationsService.MyApplication;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.job.application.JobView;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.infrastructure.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/me/applications")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Candidate portal")
public class MyApplicationsController {

    private final MyApplicationsService myApplications;

    public MyApplicationsController(MyApplicationsService myApplications) {
        this.myApplications = myApplications;
    }

    @GetMapping
    @Operation(summary = "Lists the signed-in candidate's applications with their current stage")
    public PageResponse<MyApplicationResponse> list(
            @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(myApplications.list(pageable), MyApplicationResponse::from);
    }

    /** What a candidate sees about their application. AI results are deliberately not included. */
    public record MyApplicationResponse(UUID id, UUID jobId, String jobTitle, String jobLocation, WorkMode workMode,
            boolean jobOpen, ApplicationStage stage, Instant appliedAt) {

        static MyApplicationResponse from(MyApplication item) {
            JobView job = item.job();
            return new MyApplicationResponse(item.application().getId(), item.application().getJobId(),
                    job == null ? null : job.job().getTitle(), job == null ? null : job.job().getLocation(),
                    job == null ? null : job.job().getWorkMode(), job != null && job.job().isPublished(),
                    item.application().getStage(), item.application().getAppliedAt());
        }
    }
}
