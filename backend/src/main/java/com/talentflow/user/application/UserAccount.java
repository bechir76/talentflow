package com.talentflow.user.application;

import java.util.Set;

import com.talentflow.shared.domain.Role;

/**
 * A login account as seen by TalentFlow administration.
 */
public record UserAccount(String id, String email, String firstName, String lastName, boolean enabled,
        Set<Role> roles) {

    public UserAccount {
        roles = Set.copyOf(roles);
    }

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).strip();
    }
}
