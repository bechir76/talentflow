package com.talentflow.user.infrastructure.web;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.shared.domain.Role;
import com.talentflow.shared.infrastructure.web.PageResponse;
import com.talentflow.user.application.UserAccount;
import com.talentflow.user.application.UserAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Administration")
public class AdminUserController {

    private final UserAdminService users;

    public AdminUserController(UserAdminService users) {
        this.users = users;
    }

    @GetMapping("/api/v1/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lists login accounts with their TalentFlow role")
    public PageResponse<UserResponse> list(@RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(users.find(search, pageable), UserResponse::from);
    }

    @PutMapping("/api/v1/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@PathVariable String id, @Valid @RequestBody RoleRequest request) {
        users.changeRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/admin/users/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setEnabled(@PathVariable String id, @Valid @RequestBody EnabledRequest request) {
        users.setEnabled(id, request.enabled());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/interviewers")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Hiring managers and recruiters who can be invited to interviews")
    public List<UserResponse> interviewers() {
        return users.interviewers().stream().map(UserResponse::from).toList();
    }

    public record RoleRequest(@NotNull Role role) {
    }

    public record EnabledRequest(@NotNull Boolean enabled) {
    }

    public record UserResponse(String id, String email, String fullName, boolean enabled, Set<Role> roles) {

        static UserResponse from(UserAccount account) {
            return new UserResponse(account.id(), account.email(), account.fullName(), account.enabled(),
                    account.roles());
        }
    }
}
