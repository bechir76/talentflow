import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { Job, WorkMode } from "@/features/jobs/api/jobs";
import { api } from "@/lib/api/client";
import { toQuery, type Page } from "@/lib/api/types";

export type PublicJob = Pick<Job, "id" | "title" | "description" | "requiredSkills" | "location" | "workMode"
  | "salaryMin" | "salaryMax" | "currency" | "departmentName"> & { postedAt: string };

export type PublicJobSummary = Omit<PublicJob, "description">;

export function usePublicJobs(params: { q?: string; workMode?: WorkMode; page: number }) {
  return useQuery({
    queryKey: ["public-jobs", "list", params],
    queryFn: ({ signal }) => api.get<Page<PublicJobSummary>>(`/public/jobs${toQuery({ ...params, size: 12 })}`, signal),
    placeholderData: keepPreviousData,
  });
}

export function usePublicJob(id: string | undefined) {
  return useQuery({
    queryKey: ["public-jobs", "detail", id],
    queryFn: ({ signal }) => api.get<PublicJob>(`/public/jobs/${id}`, signal),
    enabled: Boolean(id),
  });
}
