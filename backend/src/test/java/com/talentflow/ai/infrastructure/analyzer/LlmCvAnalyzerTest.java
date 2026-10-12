package com.talentflow.ai.infrastructure.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;
import com.talentflow.ai.domain.InvalidAnalysisException;

import tools.jackson.databind.json.JsonMapper;

class LlmCvAnalyzerTest {

    private static final AnalysisRequest REQUEST = new AnalysisRequest("Java Developer", "Build services.",
            List.of("Java", "Kubernetes"), "[name] has 6 years of Java experience.");

    private MockRestServiceServer server;
    private LlmCvAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://llm.example/v1")
                .defaultHeader("Authorization", "Bearer test-key");
        server = MockRestServiceServer.bindTo(builder).build();
        analyzer = new LlmCvAnalyzer(builder.build(), "test-model", JsonMapper.builder().build());
    }

    @Test
    void sendsJobAndRedactedCvAndParsesStrictJson() {
        server.expect(requestTo("https://llm.example/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"json_object\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("[name] has 6 years")))
                .andRespond(withSuccess(chatAnswer("""
                        {"score": 72, "matchingSkills": ["Java"], "missingSkills": ["Kubernetes"],
                         "yearsOfExperience": 6, "summary": "Solid Java background. No Kubernetes mentioned."}
                        """), MediaType.APPLICATION_JSON));

        CvAnalysis analysis = analyzer.analyze(REQUEST);

        assertThat(analysis.score()).isEqualTo(72);
        assertThat(analysis.missingSkills()).containsExactly("Kubernetes");
        assertThat(analyzer.modelName()).isEqualTo("test-model");
        server.verify();
    }

    @Test
    void rejectsAnswerThatIsNotJson() {
        server.expect(requestTo("https://llm.example/v1/chat/completions"))
                .andRespond(withSuccess(chatAnswer("Sure! The candidate looks great."), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> analyzer.analyze(REQUEST)).isInstanceOf(InvalidAnalysisException.class);
    }

    @Test
    void rejectsJsonOutsideTheContract() {
        assertThatThrownBy(() -> analyzer.parse("{\"score\": \"high\", \"matchingSkills\": [], \"missingSkills\": [], \"summary\": \"Ok.\"}"))
                .isInstanceOf(InvalidAnalysisException.class);
        assertThatThrownBy(() -> analyzer.parse("{\"matchingSkills\": [], \"missingSkills\": [], \"summary\": \"Ok.\"}"))
                .isInstanceOf(InvalidAnalysisException.class).hasMessageContaining("score");
        assertThatThrownBy(() -> analyzer.parse("{\"score\": 101, \"matchingSkills\": [], \"missingSkills\": [], \"summary\": \"Ok.\"}"))
                .isInstanceOf(InvalidAnalysisException.class);
    }

    @Test
    void reportsProviderErrorsAsInvalidAnalysis() {
        server.expect(requestTo("https://llm.example/v1/chat/completions")).andRespond(withServerError());

        assertThatThrownBy(() -> analyzer.analyze(REQUEST))
                .isInstanceOf(InvalidAnalysisException.class)
                .hasMessageContaining("could not be reached");
    }

    private static String chatAnswer(String content) {
        String escaped = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "{\"choices\": [{\"message\": {\"role\": \"assistant\", \"content\": \"" + escaped + "\"}}]}";
    }
}
