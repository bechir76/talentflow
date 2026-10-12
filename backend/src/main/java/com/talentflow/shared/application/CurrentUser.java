package com.talentflow.shared.application;

import java.util.Set;

import com.talentflow.shared.domain.Role;

/**
 * The authenticated user making the current request.
 *
 * @param id       the identity provider's subject id (Keycloak user id)
 * @param email    email address, may be {@code null}
 * @param fullName display name, may be {@code null}
 * @param roles    application roles granted to the user
 */
public record CurrentUser(String id, String email, String fullName, Set<Role> roles) {

    public CurrentUser {
        roles = Set.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
