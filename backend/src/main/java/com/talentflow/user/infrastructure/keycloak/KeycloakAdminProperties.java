package com.talentflow.user.infrastructure.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection to the Keycloak Admin REST API with a confidential service-account client.
 *
 * @param baseUrl      Keycloak root URL reachable from the backend, for example {@code http://keycloak:8080}
 * @param realm        realm that holds the TalentFlow users
 * @param clientId     service-account client with the realm-management user roles
 * @param clientSecret secret of that client
 */
@ConfigurationProperties("talentflow.keycloak-admin")
public record KeycloakAdminProperties(String baseUrl, String realm, String clientId, String clientSecret) {
}
