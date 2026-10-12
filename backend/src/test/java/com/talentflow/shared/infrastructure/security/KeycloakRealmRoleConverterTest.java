package com.talentflow.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsKnownRealmRolesToAuthorities() {
        Jwt jwt = jwtWithClaim("realm_access", Map.of("roles", List.of("RECRUITER", "ADMIN")));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_RECRUITER", "ROLE_ADMIN");
    }

    @Test
    void ignoresKeycloakBuiltInRoles() {
        Jwt jwt = jwtWithClaim("realm_access",
                Map.of("roles", List.of("offline_access", "uma_authorization", "default-roles-talentflow", "CANDIDATE")));

        assertThat(converter.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CANDIDATE");
    }

    @Test
    void returnsNoAuthoritiesWithoutRealmAccessClaim() {
        Jwt jwt = jwtWithClaim("scope", "openid");

        assertThat(converter.convert(jwt)).isEmpty();
    }

    private static Jwt jwtWithClaim(String name, Object value) {
        return Jwt.withTokenValue("token").header("alg", "none").subject("user-1").claim(name, value).build();
    }
}
