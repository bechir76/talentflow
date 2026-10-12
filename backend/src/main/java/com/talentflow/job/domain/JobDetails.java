package com.talentflow.job.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * The editable content of a job posting. Validates the rules that span several fields.
 */
public record JobDetails(
        String title,
        String description,
        List<String> requiredSkills,
        String location,
        WorkMode workMode,
        Integer salaryMin,
        Integer salaryMax,
        UUID departmentId) {

    public static final int MAX_SKILLS = 30;

    public JobDetails {
        title = title.strip();
        description = description.strip();
        location = location.strip();
        requiredSkills = normalizeSkills(requiredSkills);
        if (requiredSkills.isEmpty()) {
            throw new BusinessRuleException("A job needs at least one required skill.");
        }
        if (requiredSkills.size() > MAX_SKILLS) {
            throw new BusinessRuleException("A job can have at most " + MAX_SKILLS + " required skills.");
        }
        if ((salaryMin != null && salaryMin < 0) || (salaryMax != null && salaryMax < 0)) {
            throw new BusinessRuleException("Salaries cannot be negative.");
        }
        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            throw new BusinessRuleException("The minimum salary cannot be higher than the maximum salary.");
        }
    }

    /** Trims skills and drops blanks and case-insensitive duplicates, keeping the first spelling. */
    private static List<String> normalizeSkills(List<String> skills) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String skill : skills) {
            String trimmed = skill == null ? "" : skill.strip();
            if (!trimmed.isEmpty()) {
                unique.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
            }
        }
        return List.copyOf(unique.values());
    }
}
