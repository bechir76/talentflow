package com.talentflow.user.infrastructure.keycloak;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.talentflow.shared.domain.Role;
import com.talentflow.user.application.UserAccount;
import com.talentflow.user.application.UserDirectory;

/**
 * {@link UserDirectory} backed by the Keycloak Admin REST API. Uses the client credentials flow and caches the
 * access token until shortly before it expires.
 */
public class KeycloakUserDirectory implements UserDirectory {

    private static final int TOKEN_SAFETY_SECONDS = 30;
    private static final int ROLE_MEMBERS_LIMIT = 1000;

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;
    private final Clock clock;
    private String token;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public KeycloakUserDirectory(RestClient restClient, KeycloakAdminProperties properties, Clock clock) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public List<UserAccount> find(String search, int page, int size) {
        KeycloakUser[] users = admin().get()
                .uri(uri -> uri.path(realmPath("/users")).queryParam("first", page * size).queryParam("max", size)
                        .queryParamIfPresent("search", Optional.ofNullable(blankToNull(search)))
                        .queryParam("briefRepresentation", true).build())
                .retrieve().body(KeycloakUser[].class);
        Map<String, Set<Role>> roles = roleMembership();
        return Arrays.stream(users == null ? new KeycloakUser[0] : users)
                .map(user -> user.toAccount(roles.getOrDefault(user.id(), Set.of())))
                .toList();
    }

    @Override
    public long count(String search) {
        Long count = admin().get()
                .uri(uri -> uri.path(realmPath("/users/count"))
                        .queryParamIfPresent("search", Optional.ofNullable(blankToNull(search))).build())
                .retrieve().body(Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public List<UserAccount> withRole(Role role) {
        KeycloakUser[] users = admin().get()
                .uri(uri -> uri.path(realmPath("/roles/" + role.name() + "/users"))
                        .queryParam("first", 0).queryParam("max", ROLE_MEMBERS_LIMIT).build())
                .retrieve().body(KeycloakUser[].class);
        return Arrays.stream(users == null ? new KeycloakUser[0] : users)
                .map(user -> user.toAccount(Set.of(role))).toList();
    }

    @Override
    public void assignRole(String userId, Role role) {
        String mappings = realmPath("/users/" + userId + "/role-mappings/realm");
        KeycloakRole[] current = admin().get().uri(mappings).retrieve().body(KeycloakRole[].class);
        List<KeycloakRole> appRoles = Arrays.stream(current == null ? new KeycloakRole[0] : current)
                .filter(r -> Role.fromName(r.name()).isPresent()).toList();
        if (!appRoles.isEmpty()) {
            admin().method(HttpMethod.DELETE).uri(mappings)
                    .contentType(MediaType.APPLICATION_JSON).body(appRoles).retrieve().toBodilessEntity();
        }
        KeycloakRole target = admin().get().uri(realmPath("/roles/" + role.name())).retrieve().body(KeycloakRole.class);
        admin().post().uri(mappings).contentType(MediaType.APPLICATION_JSON).body(List.of(target))
                .retrieve().toBodilessEntity();
    }

    @Override
    public void setEnabled(String userId, boolean enabled) {
        admin().put().uri(realmPath("/users/" + userId)).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled)).retrieve().toBodilessEntity();
    }

    /** One call per application role instead of one per user. */
    private Map<String, Set<Role>> roleMembership() {
        Map<String, Set<Role>> byUser = new HashMap<>();
        Map<Role, List<UserAccount>> members = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            members.put(role, withRole(role));
        }
        members.forEach((role, users) -> users.forEach(
                user -> byUser.computeIfAbsent(user.id(), id -> new HashSet<>()).add(role)));
        return byUser;
    }

    private RestClient admin() {
        return restClient.mutate().defaultHeader("Authorization", "Bearer " + accessToken()).build();
    }

    private synchronized String accessToken() {
        if (token == null || clock.instant().isAfter(tokenExpiresAt)) {
            var form = new LinkedMultiValueMap<String, String>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());
            TokenResponse response = restClient.post()
                    .uri("/realms/" + properties.realm() + "/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                    .retrieve().body(TokenResponse.class);
            if (response == null || response.access_token() == null) {
                throw new IllegalStateException("Keycloak returned no admin access token");
            }
            token = response.access_token();
            tokenExpiresAt = clock.instant().plusSeconds(Math.max(0, response.expires_in() - TOKEN_SAFETY_SECONDS));
        }
        return token;
    }

    private String realmPath(String path) {
        return "/admin/realms/" + properties.realm() + path;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    record TokenResponse(String access_token, long expires_in) {
    }

    record KeycloakRole(String id, String name) {
    }

    record KeycloakUser(String id, String username, String email, String firstName, String lastName, Boolean enabled) {

        UserAccount toAccount(Set<Role> roles) {
            return new UserAccount(id, email != null ? email : username, firstName, lastName,
                    enabled == null || enabled, roles);
        }
    }
}
