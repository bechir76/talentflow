package com.talentflow.ai.infrastructure.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;

class KeywordCvAnalyzerTest {

    private final KeywordCvAnalyzer analyzer =
            new KeywordCvAnalyzer(Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void scoresSkillCoverageAndStatedExperience() {
        CvAnalysis analysis = analyzer.analyze(request(List.of("Java", "Spring Boot", "Kubernetes", "C++"),
                "Senior engineer with 8+ years of experience in java, Spring Boot and c++."));

        assertThat(analysis.matchingSkills()).containsExactly("Java", "Spring Boot", "C++");
        assertThat(analysis.missingSkills()).containsExactly("Kubernetes");
        assertThat(analysis.yearsOfExperience()).isEqualTo(8);
        // 85 * 3/4 + 15 * 1 = 78.75
        assertThat(analysis.score()).isEqualTo(79);
        assertThat(analysis.summary()).contains("3 of 4", "Kubernetes", "8 years");
    }

    @Test
    void isDeterministic() {
        AnalysisRequest request = request(List.of("Java"), "Java developer since 2019.");

        assertThat(analyzer.analyze(request)).isEqualTo(analyzer.analyze(request));
    }

    @Test
    void estimatesExperienceFromEarliestYear() {
        CvAnalysis analysis = analyzer.analyze(request(List.of("SQL"), "Analyst 2020 - present, Intern 2018."));

        assertThat(analysis.yearsOfExperience()).isEqualTo(8);
    }

    @Test
    void doesNotMatchSkillsInsideOtherWords() {
        assertThat(KeywordCvAnalyzer.mentions("I like JavaScript", "Java")).isFalse();
        assertThat(KeywordCvAnalyzer.mentions("Built with Node.js", "Node.js")).isTrue();
        assertThat(KeywordCvAnalyzer.mentions("Expert in C#", "C")).isFalse();
    }

    @Test
    void handlesCvWithoutAnyMatch() {
        CvAnalysis analysis = analyzer.analyze(request(List.of("Go"), "Chef and baker."));

        assertThat(analysis.score()).isZero();
        assertThat(analysis.yearsOfExperience()).isNull();
    }

    private static AnalysisRequest request(List<String> skills, String cv) {
        return new AnalysisRequest("Engineer", "Build things.", skills, cv);
    }
}
