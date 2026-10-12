package com.talentflow.ai.infrastructure.analyzer;

import java.net.http.HttpClient;
import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.talentflow.ai.application.CvAnalyzer;

import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    /** Uses the LLM when an API key is configured, otherwise the deterministic keyword analyzer. */
    @Bean
    CvAnalyzer cvAnalyzer(AiProperties properties, JsonMapper jsonMapper, Clock clock) {
        if (!properties.llmConfigured()) {
            log.info("No AI API key configured: using the offline keyword analyzer");
            return new KeywordCvAnalyzer(clock);
        }
        log.info("Using LLM analyzer {} at {}", properties.model(), properties.baseUrl());
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .requestFactory(requestFactory)
                .build();
        return new LlmCvAnalyzer(restClient, properties.model(), jsonMapper);
    }
}
