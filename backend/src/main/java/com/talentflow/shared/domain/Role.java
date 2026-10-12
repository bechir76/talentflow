package com.talentflow.shared.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Application roles. They mirror the Keycloak realm roles one to one.
 */
public enum Role {
    CANDIDATE,
    RECRUITER,
    HIRING_MANAGER,
    ADMIN;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + name();
    }

    public static Optional<Role> fromName(String name) {
        return Arrays.stream(values()).filter(role -> role.name().equals(name)).findFirst();
    }

    public static Optional<Role> fromAuthority(String authority) {
        if (authority == null || !authority.startsWith(AUTHORITY_PREFIX)) {
            return Optional.empty();
        }
        return fromName(authority.substring(AUTHORITY_PREFIX.length()));
    }
}
