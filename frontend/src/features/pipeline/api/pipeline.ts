import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ApplicationStage } from "@/features/applications/api/applications";
import type { AiStatus } from "@/features/candidates/api/applications";
import { api } from "@/lib/api/client";
import { toQuery, type Page } from "@/lib/api/types";

export const BOARD_STAGES: ApplicationStage[] = ["APPLIED", "SCREENING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"];

export type PipelineCard = {
  id: string;
  candidateName: string | null;
  candidateLocation: string | null;
  stage: ApplicationStage;
  stageChangedAt: string;
  appliedAt: string;
  aiStatus: AiStatus;
  aiScore: number | null;
  topSkills: string[];
};

export type StageChange = {
  id: string;
  fromStage: ApplicationStage;
  toStage: ApplicationStage;
  changedByName: string | null;
  changedAt: string;
  note: string | null;
};

/** Mirrors the backend rules so the board can refuse impossible moves before calling the API. */
export function canMove(from: ApplicationStage, to: ApplicationStage): boolean {
  if (from === to || from === "HIRED" || from === "REJECTED") return false;
  return to !== "HIRED" || from === "OFFER";
}

const boardKey = (jobId: string) => ["pipeline", jobId] as const;

export function usePipelineBoard(jobId: string | undefined) {
  return useQuery({
    queryKey: boardKey(jobId ?? ""),
    queryFn: ({ signal }) => api.get<Page<PipelineCard>>(`/jobs/${jobId}/applications${toQuery({ size: 100 })}`, signal),
    enabled: Boolean(jobId),
  });
}

export function useStageHistory(applicationId: string) {
  return useQuery({
    queryKey: ["applications", applicationId, "stage-changes"],
    queryFn: ({ signal }) => api.get<StageChange[]>(`/applications/${applicationId}/stage-changes`, signal),
  });
}

type MoveInput = { applicationId: string; stage: ApplicationStage; note?: string };

/** Moves a card immediately and rolls back if the server refuses the move. */
export function useMoveStage(jobId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ applicationId, stage, note }: MoveInput) =>
      api.patch<StageChange>(`/applications/${applicationId}/stage`, { stage, note }),
    onMutate: async ({ applicationId, stage }) => {
      if (!jobId) return undefined;
      await queryClient.cancelQueries({ queryKey: boardKey(jobId) });
      const previous = queryClient.getQueryData<Page<PipelineCard>>(boardKey(jobId));
      queryClient.setQueryData<Page<PipelineCard>>(boardKey(jobId), (page) => page && {
        ...page,
        content: page.content.map((card) => card.id === applicationId ? { ...card, stage, stageChangedAt: new Date().toISOString() } : card),
      });
      return { previous };
    },
    onError: (_error, _input, context) => {
      if (jobId && context?.previous) queryClient.setQueryData(boardKey(jobId), context.previous);
    },
    onSettled: (_data, _error, { applicationId }) => Promise.all([
      jobId ? queryClient.invalidateQueries({ queryKey: boardKey(jobId) }) : undefined,
      queryClient.invalidateQueries({ queryKey: ["applications", applicationId] }),
      queryClient.invalidateQueries({ queryKey: ["dashboard"] }),
      queryClient.invalidateQueries({ queryKey: ["jobs"] }),
    ]),
  });
}
