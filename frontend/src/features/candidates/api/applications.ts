import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ApplicationStage } from "@/features/applications/api/applications";
import type { WorkMode } from "@/features/jobs/api/jobs";
import { api } from "@/lib/api/client";

export type AiStatus = "PENDING" | "DONE" | "FAILED";

export type ApplicationDetail = {
  id: string;
  stage: ApplicationStage;
  appliedAt: string;
  coverLetter: string | null;
  cvAvailable: boolean;
  candidate: { id: string; fullName: string; email: string; phone: string | null; location: string | null };
  job: { id: string; title: string; location: string; workMode: WorkMode; departmentName: string };
  ai: {
    status: AiStatus;
    score: number | null;
    matchingSkills: string[];
    missingSkills: string[];
    yearsOfExperience: number | null;
    summary: string | null;
    model: string | null;
    analyzedAt: string | null;
  };
  consent: { givenAt: string; policyVersion: string } | null;
};

const POLL_WHILE_PENDING_MS = 3000;

export const applicationKey = (id: string) => ["applications", id] as const;

/** Polls while the AI screening is still running so the card updates by itself. */
export function useApplication(id: string | undefined) {
  return useQuery({
    queryKey: applicationKey(id ?? ""),
    queryFn: ({ signal }) => api.get<ApplicationDetail>(`/applications/${id}`, signal),
    enabled: Boolean(id),
    refetchInterval: (query) => (query.state.data?.ai.status === "PENDING" ? POLL_WHILE_PENDING_MS : false),
  });
}

export function useRerunAiAnalysis(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.post<void>(`/applications/${id}/ai-analysis`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: applicationKey(id) }),
  });
}

/** Loads the CV with the access token and exposes it as a temporary object URL for the PDF viewer. */
export function useCvUrl(id: string, enabled: boolean) {
  const cv = useQuery({
    queryKey: [...applicationKey(id), "cv"],
    queryFn: ({ signal }) => api.blob(`/applications/${id}/cv`, signal),
    enabled,
    staleTime: Infinity,
  });
  const [url, setUrl] = useState<string>();
  useEffect(() => {
    if (!cv.data) return;
    const objectUrl = URL.createObjectURL(cv.data);
    setUrl(objectUrl);
    return () => URL.revokeObjectURL(objectUrl);
  }, [cv.data]);
  return { url, size: cv.data?.size, isLoading: cv.isLoading, isError: cv.isError };
}
