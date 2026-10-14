package com.talentflow.gdpr.infrastructure.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.gdpr.application.RetentionService;
import com.talentflow.gdpr.domain.RetentionPolicy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Facts the public privacy notice shows, so the page always matches the current configuration. */
@RestController
@Tag(name = "GDPR")
public class PrivacyPolicyController {

    private final RetentionService retention;
    private final String policyVersion;

    public PrivacyPolicyController(RetentionService retention,
            @Value("${talentflow.privacy.policy-version}") String policyVersion) {
        this.retention = retention;
        this.policyVersion = policyVersion;
    }

    @GetMapping("/api/v1/public/privacy-policy")
    @Operation(summary = "Current privacy policy version and retention period for rejected candidates")
    public PrivacyPolicyResponse privacyPolicy() {
        RetentionPolicy policy = retention.policy();
        return new PrivacyPolicyResponse(policyVersion, policy.getRejectedRetentionMonths(),
                policy.isAutoDeleteEnabled());
    }

    public record PrivacyPolicyResponse(String version, int rejectedRetentionMonths, boolean autoDeleteEnabled) {
    }
}
