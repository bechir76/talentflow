import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { WorkMode } from "@/features/jobs/api/jobs";
import { api } from "@/lib/api/client";
import { toQuery, type Page } from "@/lib/api/types";

export const STAGES = ["APPLIED", "SCREENING", "INTERVIEW", "OFFER", "HIRED"] as const;
export type ApplicationStage = (typeof STAGES)[number] | "REJECTED";

export type SubmittedApplication = { id: string; jobId: string; stage: ApplicationStage; appliedAt: string };

export type MyApplication = {
  id: string;
  jobId: string;
  jobTitle: string | null;
  jobLocation: string | null;
  workMode: WorkMode | null;
  jobOpen: boolean;
  stage: ApplicationStage;
  appliedAt: string;
};

export type ApplicationInput = {
  cv: File;
  phone?: string;
  location?: string;
  coverLetter?: string;
  consent: boolean;
};

const MY_APPLICATIONS_KEY = ["my-applications"] as const;

export function useApply(jobId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: ApplicationInput) => {
      const form = new FormData();
      form.append("cv", input.cv);
      form.append("consent", String(input.consent));
      if (input.phone) form.append("phone", input.phone);
      if (input.location) form.append("location", input.location);
      if (input.coverLetter) form.append("coverLetter", input.coverLetter);
      return api.post<SubmittedApplication>(`/jobs/${jobId}/applications`, form);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: MY_APPLICATIONS_KEY }),
  });
}

export function useMyApplications(page: number) {
  return useQuery({
    queryKey: [...MY_APPLICATIONS_KEY, page],
    queryFn: ({ signal }) => api.get<Page<MyApplication>>(`/me/applications${toQuery({ page, size: 10 })}`, signal),
    placeholderData: keepPreviousData,
  });
}
