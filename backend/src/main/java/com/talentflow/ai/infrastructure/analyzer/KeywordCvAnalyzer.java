package com.talentflow.ai.infrastructure.analyzer;

import java.time.Clock;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.talentflow.ai.application.CvAnalyzer;
import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;

/**
 * Deterministic offline analyzer used when no LLM is configured (local development, tests, demos).
 * Score = 85 % skill coverage + 15 % experience (full marks from five years).
 */
public class KeywordCvAnalyzer implements CvAnalyzer {

    static final String MODEL_NAME = "keyword-matcher-v1";

    private static final double SKILL_WEIGHT = 0.85;
    private static final double EXPERIENCE_WEIGHT = 0.15;
    private static final int FULL_EXPERIENCE_YEARS = 5;
    private static final int EARLIEST_CAREER_YEAR = 1970;
    private static final Pattern STATED_YEARS =
            Pattern.compile("(?i)(\\d{1,2})\\s*\\+?\\s*(?:years|yrs|jahre)");
    private static final Pattern CALENDAR_YEAR = Pattern.compile("(?<!\\d)(19[7-9]\\d|20\\d{2})(?!\\d)");

    private final Clock clock;

    public KeywordCvAnalyzer(Clock clock) {
        this.clock = clock;
    }

    @Override
    public CvAnalysis analyze(AnalysisRequest request) {
        String text = request.redactedCvText();
        List<String> matching = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : request.requiredSkills()) {
            (mentions(text, skill) ? matching : missing).add(skill);
        }
        Integer years = yearsOfExperience(text);
        double coverage = request.requiredSkills().isEmpty() ? 0 : (double) matching.size() / request.requiredSkills().size();
        double experience = years == null ? 0 : Math.min(1.0, (double) years / FULL_EXPERIENCE_YEARS);
        int score = (int) Math.round(100 * (SKILL_WEIGHT * coverage + EXPERIENCE_WEIGHT * experience));
        return new CvAnalysis(score, matching, missing, years, summary(matching, missing, years));
    }

    @Override
    public String modelName() {
        return MODEL_NAME;
    }

    /** Whole-word, case-insensitive match that also works for skills like "C++" or "Node.js". */
    static boolean mentions(String text, String skill) {
        String pattern = "(?<![\\p{L}\\p{N}])" + Pattern.quote(skill.toLowerCase(Locale.ROOT)) + "(?![\\p{L}\\p{N}+#])";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).find();
    }

    /** Uses an explicit "8+ years" if present, otherwise the span since the earliest year in the CV. */
    Integer yearsOfExperience(String text) {
        Matcher stated = STATED_YEARS.matcher(text);
        int best = -1;
        while (stated.find()) {
            best = Math.max(best, Integer.parseInt(stated.group(1)));
        }
        if (best >= 0) {
            return best;
        }
        int currentYear = Year.now(clock).getValue();
        Matcher calendar = CALENDAR_YEAR.matcher(text);
        int earliest = Integer.MAX_VALUE;
        while (calendar.find()) {
            int year = Integer.parseInt(calendar.group(1));
            if (year >= EARLIEST_CAREER_YEAR && year <= currentYear) {
                earliest = Math.min(earliest, year);
            }
        }
        return earliest == Integer.MAX_VALUE ? null : currentYear - earliest;
    }

    private static String summary(List<String> matching, List<String> missing, Integer years) {
        int total = matching.size() + missing.size();
        StringBuilder summary = new StringBuilder("The CV mentions ").append(matching.size()).append(" of ")
                .append(total).append(" required skills");
        summary.append(matching.isEmpty() ? "." : " (" + String.join(", ", matching) + ").");
        if (!missing.isEmpty()) {
            summary.append(" Not found: ").append(String.join(", ", missing)).append(".");
        }
        summary.append(years == null ? " No experience period could be identified."
                : " It indicates about " + years + " years of experience.");
        return summary.toString();
    }
}
