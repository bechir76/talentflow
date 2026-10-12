package com.talentflow.user.application;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.Role;

/**
 * User administration for admins. Accounts live in Keycloak; changes are audit-logged here.
 */
@Service
public class UserAdminService {

    private final UserDirectory directory;
    private final AuditService audit;
    private final CurrentUserProvider currentUser;

    public UserAdminService(UserDirectory directory, AuditService audit, CurrentUserProvider currentUser) {
        this.directory = directory;
        this.audit = audit;
        this.currentUser = currentUser;
    }

    public Page<UserAccount> find(String search, Pageable pageable) {
        List<UserAccount> users = directory.find(search, pageable.getPageNumber(), pageable.getPageSize());
        return new PageImpl<>(users, pageable, directory.count(search));
    }

    /** Staff who can be invited to interviews. */
    public List<UserAccount> interviewers() {
        return Stream.of(Role.HIRING_MANAGER, Role.RECRUITER)
                .flatMap(role -> directory.withRole(role).stream())
                .filter(UserAccount::enabled)
                .distinct()
                .toList();
    }

    @Transactional
    public void changeRole(String userId, Role role) {
        requireOtherUser(userId, "change your own role");
        directory.assignRole(userId, role);
        audit.record(AuditAction.USER_ROLE_CHANGED, "User", null, userId + " -> " + role);
    }

    @Transactional
    public void setEnabled(String userId, boolean enabled) {
        requireOtherUser(userId, "disable your own account");
        directory.setEnabled(userId, enabled);
        audit.record(AuditAction.USER_STATUS_CHANGED, "User", null, userId + (enabled ? " enabled" : " disabled"));
    }

    /** Prevents admins from locking themselves out. */
    private void requireOtherUser(String userId, String action) {
        if (currentUser.currentUser().id().equals(userId)) {
            throw new BusinessRuleException("You cannot " + action + ".");
        }
    }
}
