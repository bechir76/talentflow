package com.talentflow.gdpr.infrastructure.web;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.gdpr.application.CandidateDataExport;
import com.talentflow.gdpr.application.CandidateDataExport.ConsentRecord;
import com.talentflow.gdpr.application.MyDataService;
import com.talentflow.gdpr.application.RetentionService;
import com.talentflow.gdpr.domain.RetentionPolicy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "GDPR")
public class GdprController {

    private final MyDataService myData;
    private final RetentionService retention;

    public GdprController(MyDataService myData, RetentionService retention) {
        this.myData = myData;
        this.retention = retention;
    }

    @GetMapping("/api/v1/me/data-export")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Downloads all data stored about the signed-in candidate (GDPR Art. 15)")
    public ResponseEntity<CandidateDataExport> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("talentflow-data-export.json").build().toString())
                .body(myData.export());
    }

    @GetMapping("/api/v1/me/consents")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Consent history of the signed-in candidate")
    public List<ConsentRecord> consents() {
        return myData.consents();
    }

    @DeleteMapping("/api/v1/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Anonymises the signed-in candidate and deletes their CVs (GDPR Art. 17)")
    public ResponseEntity<Void> deleteMyData() {
        myData.deleteMyData();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/admin/retention-policy")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionPolicyResponse policy() {
        return RetentionPolicyResponse.from(retention.policy());
    }

    @PutMapping("/api/v1/admin/retention-policy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Changes how long rejected candidates are kept and whether the daily job runs")
    public RetentionPolicyResponse updatePolicy(@Valid @RequestBody RetentionPolicyRequest request) {
        return RetentionPolicyResponse.from(retention.updatePolicy(request.rejectedRetentionMonths(),
                request.autoDeleteEnabled()));
    }

    @PostMapping("/api/v1/admin/retention-policy/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Anonymises expired rejected candidates now")
    public RetentionRunResponse run() {
        return new RetentionRunResponse(retention.anonymizeExpired());
    }

    public record RetentionPolicyRequest(@Min(RetentionPolicy.MIN_MONTHS) @Max(RetentionPolicy.MAX_MONTHS) int rejectedRetentionMonths,
            boolean autoDeleteEnabled) {
    }

    public record RetentionPolicyResponse(int rejectedRetentionMonths, boolean autoDeleteEnabled, Instant updatedAt) {

        static RetentionPolicyResponse from(RetentionPolicy policy) {
            return new RetentionPolicyResponse(policy.getRejectedRetentionMonths(), policy.isAutoDeleteEnabled(),
                    policy.getUpdatedAt());
        }
    }

    public record RetentionRunResponse(int anonymizedCandidates) {
    }
}
