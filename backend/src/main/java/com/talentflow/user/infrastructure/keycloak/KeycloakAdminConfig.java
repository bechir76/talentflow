package com.talentflow.user.infrastructure.keycloak;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.talentflow.user.application.UserDirectory;

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfig {

    @Bean
    UserDirectory userDirectory(KeycloakAdminProperties properties, Clock clock) {
        return new KeycloakUserDirectory(RestClient.builder().baseUrl(properties.baseUrl()).build(), properties, clock);
    }
}
