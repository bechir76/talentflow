package com.talentflow.ai.application;

import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;

/**
 * Port for whatever produces the screening suggestion: an LLM, or the offline keyword matcher.
 */
public interface CvAnalyzer {

    CvAnalysis analyze(AnalysisRequest request);

    /** Shown to recruiters so they know which system produced a suggestion. */
    String modelName();
}
