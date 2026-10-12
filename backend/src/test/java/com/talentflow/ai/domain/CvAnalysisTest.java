package com.talentflow.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class CvAnalysisTest {

    @Test
    void acceptsValidAnalysisAndCleansSkills() {
        CvAnalysis analysis = new CvAnalysis(87, List.of(" Java ", "Java", ""), List.of("Kubernetes"), 8,
                " Strong backend profile. Good match. ");

        assertThat(analysis.matchingSkills()).containsExactly("Java");
        assertThat(analysis.summary()).isEqualTo("Strong backend profile. Good match.");
    }

    @Test
    void rejectsScoreOutsideRange() {
        assertThatThrownBy(() -> new CvAnalysis(140, List.of(), List.of(), null, "Fine."))
                .isInstanceOf(InvalidAnalysisException.class);
    }

    @Test
    void rejectsMissingSkillLists() {
        assertThatThrownBy(() -> new CvAnalysis(50, null, List.of(), null, "Fine."))
                .isInstanceOf(InvalidAnalysisException.class);
    }

    @Test
    void rejectsSummaryWithMoreThanThreeSentences() {
        assertThatThrownBy(() -> new CvAnalysis(50, List.of(), List.of(), null, "One. Two. Three. Four."))
                .isInstanceOf(InvalidAnalysisException.class)
                .hasMessageContaining("3 sentences");
    }

    @Test
    void countsSentencesWithoutTrailingPunctuation() {
        assertThat(CvAnalysis.sentenceCount("One. Two")).isEqualTo(2);
        assertThat(CvAnalysis.sentenceCount("Uses Node.js and Vue.js daily.")).isEqualTo(1);
    }
}
