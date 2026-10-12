package com.talentflow.ai.domain;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A validated AI suggestion for one application. Construction fails with {@link InvalidAnalysisException}
 * if the analyzer returned something outside the agreed contract, so bad output is never stored.
 */
public record CvAnalysis(int score, List<String> matchingSkills, List<String> missingSkills,
        Integer yearsOfExperience, String summary) {

    public static final int MAX_SENTENCES = 3;
    private static final int MAX_SKILLS = 50;
    private static final int MAX_SUMMARY_LENGTH = 800;
    private static final int MAX_YEARS = 60;
    private static final Pattern SENTENCE_END = Pattern.compile("[.!?]+(\\s+|$)");

    public CvAnalysis {
        if (score < 0 || score > 100) {
            throw new InvalidAnalysisException("score must be between 0 and 100 but was " + score);
        }
        matchingSkills = cleanSkills(matchingSkills, "matchingSkills");
        missingSkills = cleanSkills(missingSkills, "missingSkills");
        if (yearsOfExperience != null && (yearsOfExperience < 0 || yearsOfExperience > MAX_YEARS)) {
            throw new InvalidAnalysisException("yearsOfExperience is out of range: " + yearsOfExperience);
        }
        if (summary == null || summary.isBlank()) {
            throw new InvalidAnalysisException("summary is missing");
        }
        summary = summary.strip();
        if (summary.length() > MAX_SUMMARY_LENGTH || sentenceCount(summary) > MAX_SENTENCES) {
            throw new InvalidAnalysisException("summary must be at most " + MAX_SENTENCES + " sentences");
        }
    }

    static int sentenceCount(String text) {
        int sentences = 0;
        var matcher = SENTENCE_END.matcher(text);
        int lastEnd = 0;
        while (matcher.find()) {
            sentences++;
            lastEnd = matcher.end();
        }
        return lastEnd < text.length() ? sentences + 1 : sentences;
    }

    private static List<String> cleanSkills(List<String> skills, String field) {
        if (skills == null) {
            throw new InvalidAnalysisException(field + " is missing");
        }
        if (skills.size() > MAX_SKILLS) {
            throw new InvalidAnalysisException(field + " has too many entries");
        }
        return skills.stream()
                .map(skill -> skill == null ? "" : skill.strip())
                .filter(skill -> !skill.isEmpty())
                .distinct()
                .toList();
    }
}
