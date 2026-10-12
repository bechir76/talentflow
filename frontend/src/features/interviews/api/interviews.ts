import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { toQuery } from "@/lib/api/types";
import type { Role } from "@/lib/auth/roles";

export type Recommendation = "HIRE" | "NO_HIRE" | "UNSURE";
export type Interviewer = { id: string; name: string };

export type FeedbackEntry = {
  id: string;
  authorName: string | null;
  technical: number;
  communication: number;
  cultureFit: number;
  comment: string | null;
  recommendation: Recommendation;
  submittedAt: string;
};

export type Interview = {
  id: string;
  applicationId: string;
  scheduledAt: string;
  durationMinutes: number;
  interviewers: Interviewer[];
  meetingLink: string | null;
  feedback: FeedbackEntry[];
  canGiveFeedback: boolean;
};

export type CalendarEntry = Omit<Interview, "feedback" | "canGiveFeedback"> & { candidateName: string | null; jobTitle: string | null };

export type StaffUser = { id: string; email: string; fullName: string; enabled: boolean; roles: Role[] };

export type ScheduleInput = { scheduledAt: string; durationMinutes: number; interviewerIds: string[]; meetingLink?: string };

export type FeedbackInput = { technical: number; communication: number; cultureFit: number; comment?: string; recommendation: Recommendation };

const interviewsKey = (applicationId: string) => ["applications", applicationId, "interviews"] as const;

export function useApplicationInterviews(applicationId: string) {
  return useQuery({
    queryKey: interviewsKey(applicationId),
    queryFn: ({ signal }) => api.get<Interview[]>(`/applications/${applicationId}/interviews`, signal),
  });
}

export function useInterviewers(enabled: boolean) {
  return useQuery({
    queryKey: ["interviewers"],
    queryFn: ({ signal }) => api.get<StaffUser[]>("/interviewers", signal),
    enabled,
    staleTime: 5 * 60_000,
  });
}

export function useCalendar(from: Date, to: Date) {
  return useQuery({
    queryKey: ["calendar", from.toISOString(), to.toISOString()],
    queryFn: ({ signal }) => api.get<CalendarEntry[]>(`/interviews${toQuery({ from: from.toISOString(), to: to.toISOString() })}`, signal),
    placeholderData: keepPreviousData,
  });
}

function useInvalidateInterviews(applicationId: string) {
  const queryClient = useQueryClient();
  return () => Promise.all([
    queryClient.invalidateQueries({ queryKey: interviewsKey(applicationId) }),
    queryClient.invalidateQueries({ queryKey: ["calendar"] }),
    queryClient.invalidateQueries({ queryKey: ["dashboard"] }),
  ]);
}

export function useScheduleInterview(applicationId: string) {
  const invalidate = useInvalidateInterviews(applicationId);
  return useMutation({
    mutationFn: (input: ScheduleInput) => api.post<Interview>(`/applications/${applicationId}/interviews`, input),
    onSuccess: invalidate,
  });
}

export function useCancelInterview(applicationId: string) {
  const invalidate = useInvalidateInterviews(applicationId);
  return useMutation({
    mutationFn: (interviewId: string) => api.delete(`/interviews/${interviewId}`),
    onSuccess: invalidate,
  });
}

export function useSubmitFeedback(applicationId: string) {
  const invalidate = useInvalidateInterviews(applicationId);
  return useMutation({
    mutationFn: ({ interviewId, input }: { interviewId: string; input: FeedbackInput }) =>
      api.post<FeedbackEntry>(`/interviews/${interviewId}/feedback`, input),
    onSuccess: invalidate,
  });
}
