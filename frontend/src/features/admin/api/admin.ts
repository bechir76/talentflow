import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { toQuery, type Page } from "@/lib/api/types";
import type { Role } from "@/lib/auth/roles";

export type AdminUser = { id: string; email: string; fullName: string; enabled: boolean; roles: Role[] };

export type RetentionPolicy = { rejectedRetentionMonths: number; autoDeleteEnabled: boolean; updatedAt: string };

export const AUDIT_ACTIONS = ["STAGE_CHANGED", "CV_VIEWED", "CV_DOWNLOADED", "DATA_EXPORTED", "CANDIDATE_DELETED",
  "RETENTION_POLICY_CHANGED", "USER_ROLE_CHANGED", "USER_STATUS_CHANGED"] as const;
export type AuditAction = (typeof AUDIT_ACTIONS)[number];

export type AuditEntry = {
  id: string;
  timestamp: string;
  actorId: string;
  actorName: string | null;
  action: AuditAction;
  entityType: string;
  entityId: string | null;
  details: string | null;
};

export function useAdminUsers(search: string, page: number) {
  return useQuery({
    queryKey: ["admin-users", search, page],
    queryFn: ({ signal }) => api.get<Page<AdminUser>>(`/admin/users${toQuery({ search, page, size: 20 })}`, signal),
    placeholderData: keepPreviousData,
  });
}

export function useChangeUserRole() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, role }: { id: string; role: Role }) => api.put<void>(`/admin/users/${id}/role`, { role }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });
}

export function useSetUserEnabled() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) => api.put<void>(`/admin/users/${id}/enabled`, { enabled }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });
}

export function useRetentionPolicy() {
  return useQuery({
    queryKey: ["retention-policy"],
    queryFn: ({ signal }) => api.get<RetentionPolicy>("/admin/retention-policy", signal),
  });
}

export function useUpdateRetentionPolicy() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (policy: Pick<RetentionPolicy, "rejectedRetentionMonths" | "autoDeleteEnabled">) =>
      api.put<RetentionPolicy>("/admin/retention-policy", policy),
    onSuccess: (policy) => queryClient.setQueryData(["retention-policy"], policy),
  });
}

export function useRunRetention() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.post<{ anonymizedCandidates: number }>("/admin/retention-policy/run"),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["audit-log"] }),
  });
}

export function useAuditLog(action: AuditAction | undefined, page: number) {
  return useQuery({
    queryKey: ["audit-log", action, page],
    queryFn: ({ signal }) => api.get<Page<AuditEntry>>(`/admin/audit-log${toQuery({ action, page, size: 25 })}`, signal),
    placeholderData: keepPreviousData,
  });
}
