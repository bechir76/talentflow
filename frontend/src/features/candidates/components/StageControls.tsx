import type { ReactNode } from "react";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import type { ApplicationStage } from "@/features/applications/api/applications";
import { canMove, useMoveStage } from "@/features/pipeline/api/pipeline";

const FORWARD: ApplicationStage[] = ["APPLIED", "SCREENING", "INTERVIEW", "OFFER", "HIRED"];

type StageControlsProps = { applicationId: string; jobId: string; stage: ApplicationStage; canManage: boolean; children?: ReactNode };

/** Stage select, "next stage" and "reject", as in the design header. Read-only for hiring managers. */
export default function StageControls({ applicationId, jobId, stage, canManage, children }: StageControlsProps) {
  const { t } = useTranslation();
  const move = useMoveStage(jobId);
  const next = FORWARD[FORWARD.indexOf(stage) + 1];
  const finished = stage === "HIRED" || stage === "REJECTED";
  const disabled = !canManage || finished || move.isPending;
  const moveTo = (target: ApplicationStage) => move.mutate({ applicationId, stage: target });

  return <div className="stage-controls">
    <div className="candidate-actions">
      <label>{t("candidate.currentStage")}
        <select value={stage} disabled={disabled} onChange={(e) => moveTo(e.target.value as ApplicationStage)}>
          <option value={stage}>{t(`pipeline.stages.${stage}`)}</option>
          {FORWARD.filter((option) => canMove(stage, option)).map((option) => <option key={option} value={option}>{t(`pipeline.stages.${option}`)}</option>)}
        </select>
      </label>
      {children}
      {next && canMove(stage, next) && <button className="primary-button" disabled={disabled} onClick={() => moveTo(next)}>{t("candidate.moveTo", { stage: t(`pipeline.stages.${next}`) })}<Icon name="arrow"/></button>}
      {!finished && <button className="reject-button" disabled={disabled} onClick={() => moveTo("REJECTED")}>{t("candidate.reject")}</button>}
    </div>
    {move.isError && <p className="field-error stage-error" role="alert">{move.error.message}</p>}
  </div>;
}
