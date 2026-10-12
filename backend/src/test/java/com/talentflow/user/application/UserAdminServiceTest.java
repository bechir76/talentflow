package com.talentflow.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.Role;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserDirectory directory;
    @Mock
    private AuditService audit;
    @Mock
    private CurrentUserProvider currentUser;

    private UserAdminService service;

    @BeforeEach
    void setUp() {
        service = new UserAdminService(directory, audit, currentUser);
    }

    @Test
    void changesAnotherUsersRoleAndAuditsIt() {
        when(currentUser.currentUser()).thenReturn(new CurrentUser("admin-1", null, "Alex", Set.of(Role.ADMIN)));

        service.changeRole("user-2", Role.HIRING_MANAGER);

        verify(directory).assignRole("user-2", Role.HIRING_MANAGER);
        verify(audit).record(AuditAction.USER_ROLE_CHANGED, "User", null, "user-2 -> HIRING_MANAGER");
    }

    @Test
    void adminsCannotLockThemselvesOut() {
        when(currentUser.currentUser()).thenReturn(new CurrentUser("admin-1", null, "Alex", Set.of(Role.ADMIN)));

        assertThatThrownBy(() -> service.setEnabled("admin-1", false)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.changeRole("admin-1", Role.CANDIDATE)).isInstanceOf(BusinessRuleException.class);
        verify(directory, never()).setEnabled(any(), any(Boolean.class));
    }

    @Test
    void interviewersAreEnabledManagersAndRecruiters() {
        when(directory.withRole(Role.HIRING_MANAGER)).thenReturn(List.of(
                new UserAccount("m1", "m@x", "Martin", "Richter", true, Set.of(Role.HIRING_MANAGER)),
                new UserAccount("m2", "old@x", "Old", "Account", false, Set.of(Role.HIRING_MANAGER))));
        when(directory.withRole(Role.RECRUITER)).thenReturn(List.of(
                new UserAccount("r1", "r@x", "Anna", "Keller", true, Set.of(Role.RECRUITER))));

        assertThat(service.interviewers()).extracting(UserAccount::id).containsExactly("m1", "r1");
    }
}
