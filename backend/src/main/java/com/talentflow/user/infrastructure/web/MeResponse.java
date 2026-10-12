package com.talentflow.user.infrastructure.web;

import java.util.List;

import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.domain.Role;

public record MeResponse(String id, String email, String fullName, List<Role> roles) {

    static MeResponse from(CurrentUser user) {
        List<Role> roles = user.roles().stream().sorted().toList();
        return new MeResponse(user.id(), user.email(), user.fullName(), roles);
    }
}
