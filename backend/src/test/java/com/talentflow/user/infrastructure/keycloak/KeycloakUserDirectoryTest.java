package com.talentflow.user.infrastructure.keycloak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.talentflow.shared.domain.Role;
import com.talentflow.user.application.UserAccount;

class KeycloakUserDirectoryTest {

    private static final String KC = "http://kc.test";
    private static final String ADMIN = KC + "/admin/realms/talentflow";

    private MockRestServiceServer server;
    private KeycloakUserDirectory directory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(KC);
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        directory = new KeycloakUserDirectory(builder.build(),
                new KeycloakAdminProperties(KC, "talentflow", "talentflow-admin-api", "secret"),
                Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC));
        server.expect(once(), requestTo(KC + "/realms/talentflow/protocol/openid-connect/token"))
                .andExpect(content().string(Matchers.containsString("grant_type=client_credentials")))
                .andRespond(withSuccess("{\"access_token\": \"admin-token\", \"expires_in\": 300}", MediaType.APPLICATION_JSON));
    }

    @Test
    void listsUsersWithTheirApplicationRoles() {
        server.expect(requestTo(Matchers.startsWith(ADMIN + "/users?first=20&max=10&search=anna")))
                .andExpect(header("Authorization", "Bearer admin-token"))
                .andRespond(withSuccess("[{\"id\": \"u1\", \"username\": \"anna@x.dev\", \"email\": \"anna@x.dev\", \"firstName\": \"Anna\", \"lastName\": \"Keller\", \"enabled\": true}]", MediaType.APPLICATION_JSON));
        for (Role role : Role.values()) {
            server.expect(manyTimes(), requestTo(Matchers.startsWith(ADMIN + "/roles/" + role.name() + "/users")))
                    .andRespond(withSuccess(role == Role.RECRUITER ? "[{\"id\": \"u1\"}]" : "[]", MediaType.APPLICATION_JSON));
        }

        var users = directory.find("anna", 2, 10);

        assertThat(users).singleElement().satisfies(user -> {
            assertThat(user.fullName()).isEqualTo("Anna Keller");
            assertThat(user.roles()).containsExactly(Role.RECRUITER);
        });
    }

    @Test
    void replacesOnlyTheApplicationRole() {
        server.expect(requestTo(ADMIN + "/users/u1/role-mappings/realm")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\": \"r1\", \"name\": \"CANDIDATE\"}, {\"id\": \"r9\", \"name\": \"offline_access\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADMIN + "/users/u1/role-mappings/realm")).andExpect(method(HttpMethod.DELETE))
                .andExpect(content().string(Matchers.allOf(Matchers.containsString("CANDIDATE"), Matchers.not(Matchers.containsString("offline_access")))))
                .andRespond(withNoContent());
        server.expect(requestTo(ADMIN + "/roles/RECRUITER")).andRespond(withSuccess("{\"id\": \"r2\", \"name\": \"RECRUITER\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADMIN + "/users/u1/role-mappings/realm")).andExpect(method(HttpMethod.POST))
                .andExpect(content().string(Matchers.containsString("RECRUITER"))).andRespond(withNoContent());

        directory.assignRole("u1", Role.RECRUITER);

        server.verify();
    }

    @Test
    void reusesTheAccessTokenUntilItExpires() {
        server.expect(manyTimes(), requestTo(Matchers.startsWith(ADMIN + "/users/count")))
                .andRespond(withSuccess("7", MediaType.APPLICATION_JSON));

        assertThat(directory.count(null)).isEqualTo(7);
        assertThat(directory.count("x")).isEqualTo(7);
        server.verify();
    }

    @Test
    void mapsMissingFlagsSafely() {
        UserAccount account = new KeycloakUserDirectory.KeycloakUser("u2", "only-username", null, null, null, null)
                .toAccount(java.util.Set.of());

        assertThat(account.email()).isEqualTo("only-username");
        assertThat(account.enabled()).isTrue();
    }
}
