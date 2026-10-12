import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { toQuery, type Page } from "@/lib/api/types";

export type WorkMode = "REMOTE" | "HYBRID" | "ONSITE";
export type JobStatus = "DRAFT" | "PUBLISHED" | "CLOSED";

export type Job = {
  id: string;
  title: string;
  description: string;
  requiredSkills: string[];
  location: string;
  workMode: WorkMode;
  salaryMin: number | null;
  salaryMax: number | null;
  currency: string;
  status: JobStatus;
  departmentId: string;
  departmentName: string;
  createdAt: string;
  updatedAt: string;
};

export type JobSummary = Pick<Job, "id" | "title" | "departmentName" | "location" | "workMode" | "status" | "createdAt">
  & { applicantCount: number };

export type JobStats = { published: number; drafts: number; closed: number; activeApplicants: number };

export type Department = { id: string; name: string };

export type JobInput = {
  title: string;
  description: string;
  requiredSkills: string[];
  location: string;
  workMode: WorkMode;
  salaryMin?: number;
  salaryMax?: number;
  departmentId: string;
  status?: JobStatus;
};

export type JobListParams = { q?: string; status?: JobStatus; departmentId?: string; page: number };

const JOBS_KEY = ["jobs"] as const;

export function useJobs(params: JobListParams) {
  return useQuery({
    queryKey: [...JOBS_KEY, "list", params],
    queryFn: ({ signal }) => api.get<Page<JobSummary>>(`/jobs${toQuery({ ...params, size: 10 })}`, signal),
    placeholderData: keepPreviousData,
  });
}

export function useJobStats() {
  return useQuery({
    queryKey: [...JOBS_KEY, "stats"],
    queryFn: ({ signal }) => api.get<JobStats>("/jobs/stats", signal),
  });
}

export function useJob(id: string | undefined) {
  return useQuery({
    queryKey: [...JOBS_KEY, "detail", id],
    queryFn: ({ signal }) => api.get<Job>(`/jobs/${id}`, signal),
    enabled: Boolean(id),
  });
}

export function useDepartments() {
  return useQuery({
    queryKey: ["departments"],
    queryFn: ({ signal }) => api.get<Department[]>("/departments", signal),
    staleTime: 5 * 60_000,
  });
}

/** Every job mutation refreshes lists, stats and the career page. */
function useInvalidateJobs() {
  const queryClient = useQueryClient();
  return () => Promise.all([
    queryClient.invalidateQueries({ queryKey: JOBS_KEY }),
    queryClient.invalidateQueries({ queryKey: ["public-jobs"] }),
  ]);
}

export function useCreateJob() {
  const invalidate = useInvalidateJobs();
  return useMutation({
    mutationFn: (input: JobInput) => api.post<Job>("/jobs", input),
    onSuccess: invalidate,
  });
}

export function useUpdateJob(id: string) {
  const invalidate = useInvalidateJobs();
  return useMutation({
    mutationFn: (input: JobInput) => api.put<Job>(`/jobs/${id}`, input),
    onSuccess: invalidate,
  });
}

export function useChangeJobStatus() {
  const invalidate = useInvalidateJobs();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: JobStatus }) => api.patch<Job>(`/jobs/${id}/status`, { status }),
    onSuccess: invalidate,
  });
}
