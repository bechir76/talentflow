package com.talentflow.ai.infrastructure.analyzer;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LLM provider settings. Any provider with an OpenAI-compatible chat completions API works
 * (for example Mistral, OpenAI, Azure OpenAI or a local Ollama). Without an API key the
 * offline keyword analyzer is used.
 *
 * @param baseUrl API root, for example {@code https://api.mistral.ai/v1}
 * @param apiKey  secret key; empty means "use the offline analyzer"
 * @param model   model name sent to the provider
 * @param timeout maximum time to wait for one analysis
 */
@ConfigurationProperties("talentflow.ai")
public record AiProperties(String baseUrl, String apiKey, String model, Duration timeout) {

    public boolean llmConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
