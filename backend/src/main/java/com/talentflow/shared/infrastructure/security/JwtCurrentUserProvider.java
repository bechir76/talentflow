package com.talentflow.shared.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.Role;

/**
 * Reads the current user from the Keycloak access token in the security context.
 */
@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated user in the security context");
        }
        Set<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .flatMap(authority -> Role.fromAuthority(authority).stream())
                .collect(Collectors.toSet());
        return new CurrentUser(jwt.getSubject(), jwt.getClaimAsString("email"), jwt.getClaimAsString("name"), roles);
    }
}
