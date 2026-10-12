import { useDroppable } from "@dnd-kit/core";
import type { ReactNode } from "react";
import { useTranslation } from "react-i18next";
import type { ApplicationStage } from "@/features/applications/api/applications";

const TONES: Record<ApplicationStage, string> = {
  APPLIED: "gray", SCREENING: "indigo", INTERVIEW: "amber", OFFER: "purple", HIRED: "green", REJECTED: "red",
};

type KanbanColumnProps = { stage: ApplicationStage; count: number; canDrop: boolean; children: ReactNode };

export default function KanbanColumn({ stage, count, canDrop, children }: KanbanColumnProps) {
  const { t } = useTranslation();
  const { setNodeRef, isOver } = useDroppable({ id: stage, disabled: !canDrop });
  return <section className="kanban-column" aria-label={t(`pipeline.stages.${stage}`)}>
    <div className="column-head"><span className={`stage-dot ${TONES[stage]}`}></span><strong>{t(`pipeline.stages.${stage}`)}</strong><b>{count}</b></div>
    <div ref={setNodeRef} className={`column-cards ${isOver && canDrop ? "drop-target" : ""}`}>
      {children}
      {count === 0 && <div className="empty-column">{t("pipeline.emptyColumn")}</div>}
    </div>
  </section>;
}
