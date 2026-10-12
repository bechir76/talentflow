package com.talentflow.audit.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;
import com.talentflow.shared.infrastructure.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Administration")
public class AuditLogController {

    private final AuditService audit;

    public AuditLogController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping("/api/v1/admin/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Audit trail of sensitive actions, newest first")
    public PageResponse<AuditLogResponse> list(@RequestParam(required = false) AuditAction action,
            @PageableDefault(size = 25) Pageable pageable) {
        return PageResponse.from(audit.search(action, pageable), AuditLogResponse::from);
    }

    public record AuditLogResponse(UUID id, Instant timestamp, String actorId, String actorName, AuditAction action,
            String entityType, UUID entityId, String details) {

        static AuditLogResponse from(AuditLog entry) {
            return new AuditLogResponse(entry.getId(), entry.getTimestamp(), entry.getActorId(), entry.getActorName(),
                    entry.getAction(), entry.getEntityType(), entry.getEntityId(), entry.getDetails());
        }
    }
}
