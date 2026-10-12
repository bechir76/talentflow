package com.talentflow.application.infrastructure.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.application.application.ApplicationQueryService;
import com.talentflow.application.application.ApplicationQueryService.ApplicationDetails;
import com.talentflow.application.domain.AiStatus;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.job.domain.WorkMode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/applications/{id}")
@PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
@Tag(name = "Applications")
public class StaffApplicationController {

    private final ApplicationQueryService queries;

    public StaffApplicationController(ApplicationQueryService queries) {
        this.queries = queries;
    }

    @GetMapping
    @Operation(summary = "Returns an application with candidate, job, consent and AI suggestion")
    public ApplicationDetailResponse get(@PathVariable UUID id) {
        return ApplicationDetailResponse.from(queries.get(id));
    }

    @GetMapping("/cv")
    @Operation(summary = "Returns the CV as PDF for viewing in the browser")
    public ResponseEntity<byte[]> cv(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("cv.pdf").build().toString())
                .cacheControl(CacheControl.noStore())
                .body(queries.cv(id));
    }

    public record ApplicationDetailResponse(UUID id, ApplicationStage stage, Instant appliedAt, String coverLetter,
            boolean cvAvailable, CandidateInfo candidate, JobInfo job, AiInfo ai, ConsentInfo consent) {

        static ApplicationDetailResponse from(ApplicationDetails details) {
            JobApplication application = details.application();
            Candidate candidate = details.candidate();
            var job = details.job().job();
            return new ApplicationDetailResponse(application.getId(), application.getStage(),
                    application.getAppliedAt(), application.getCoverLetter(), application.getCvStorageKey() != null,
                    new CandidateInfo(candidate.getId(), candidate.getFullName(), candidate.getEmail(),
                            candidate.getPhone(), candidate.getLocation()),
                    new JobInfo(job.getId(), job.getTitle(), job.getLocation(), job.getWorkMode(),
                            details.job().departmentName()),
                    new AiInfo(application.getAiStatus(), application.getAiScore(), application.getAiMatchingSkills(),
                            application.getAiMissingSkills(), application.getAiYearsOfExperience(),
                            application.getAiSummary(), application.getAiModel(), application.getAiAnalyzedAt()),
                    details.consent().map(c -> new ConsentInfo(c.getGivenAt(), c.getPolicyVersion())).orElse(null));
        }
    }

    public record CandidateInfo(UUID id, String fullName, String email, String phone, String location) {
    }

    public record JobInfo(UUID id, String title, String location, WorkMode workMode, String departmentName) {
    }

    /** AI suggestion only; the decision stays with the recruiter (EU AI Act). */
    public record AiInfo(AiStatus status, Integer score, List<String> matchingSkills, List<String> missingSkills,
            Integer yearsOfExperience, String summary, String model, Instant analyzedAt) {
    }

    public record ConsentInfo(Instant givenAt, String policyVersion) {
    }
}
