package com.talentflow.ai.domain;

import java.util.List;

/**
 * Everything the analyzer may see: the job and the CV text with personal data removed.
 */
public record AnalysisRequest(String jobTitle, String jobDescription, List<String> requiredSkills,
        String redactedCvText) {

    public AnalysisRequest {
        requiredSkills = List.copyOf(requiredSkills);
    }
}
