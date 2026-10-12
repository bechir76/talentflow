export type ScoreTone = "excellent" | "good" | "low";

/** Colour band of an AI match score, shared by the Kanban card and the analysis card. */
export function scoreTone(score: number): ScoreTone {
  if (score >= 80) return "excellent";
  if (score >= 60) return "good";
  return "low";
}
