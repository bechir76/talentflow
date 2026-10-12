import { useQuery } from "@tanstack/react-query";
import type { ApplicationStage } from "@/features/applications/api/applications";
import { api } from "@/lib/api/client";

export type Dashboard = {
  openJobs: number;
  activeCandidates: number;
  interviewsThisWeek: number;
  upcomingInterviews: {
    interviewId: string;
    applicationId: string;
    candidateName: string | null;
    jobTitle: string | null;
    scheduledAt: string;
    durationMinutes: number;
    interviewerNames: string[];
  }[];
  averageDaysToHire: number | null;
  applicationsPerJob: { jobId: string; jobTitle: string; applications: number }[];
  funnel: { stage: ApplicationStage; applications: number }[];
  topMatches: { applicationId: string; candidateName: string; jobTitle: string; score: number }[];
};

export function useDashboard() {
  return useQuery({
    queryKey: ["dashboard"],
    queryFn: ({ signal }) => api.get<Dashboard>("/dashboard", signal),
  });
}
