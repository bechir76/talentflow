package com.talentflow.user.application;

import java.util.List;

import com.talentflow.shared.domain.Role;

/**
 * Port to the identity provider that owns the user accounts.
 */
public interface UserDirectory {

    /** Users matching {@code search} (name, email; null for all), zero-based page. */
    List<UserAccount> find(String search, int page, int size);

    long count(String search);

    List<UserAccount> withRole(Role role);

    /** Replaces the user's application role with {@code role}; other Keycloak roles stay untouched. */
    void assignRole(String userId, Role role);

    void setEnabled(String userId, boolean enabled);
}
