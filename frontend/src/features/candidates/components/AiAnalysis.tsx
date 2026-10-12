import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import { formatDate } from "@/lib/format";
import { useRerunAiAnalysis, type ApplicationDetail } from "../api/applications";

const RING_CIRCUMFERENCE = 2 * Math.PI * 52;

export type ScoreTone = "excellent" | "good" | "low";

export function scoreTone(score: number): ScoreTone {
  if (score >= 80) return "excellent";
  if (score >= 60) return "good";
  return "low";
}

/** AI screening suggestion. It supports the recruiter; the decision is always made by a person. */
export default function AiAnalysis({ applicationId, ai, canRerun }: { applicationId: string; ai: ApplicationDetail["ai"]; canRerun: boolean }) {
  const { t } = useTranslation();
  const rerun = useRerunAiAnalysis(applicationId);
  const rerunButton = canRerun && <button className="secondary-button" disabled={rerun.isPending} onClick={() => rerun.mutate()}><Icon name="sparkles" size={14}/>{t("candidate.ai.rerun")}</button>;

  return <article className="analysis-card panel">
    <div className="analysis-heading">
      <span className="sparkle-icon"><Icon name="sparkles" size={17}/></span>
      <div><h3>{t("candidate.ai.title")}</h3><p>{t("candidate.ai.subtitle")}</p></div>
      {ai.model && <span className="analysis-version" title={ai.analyzedAt ? formatDate(ai.analyzedAt) : undefined}>{ai.model}</span>}
    </div>

    {ai.status === "PENDING" && <div className="analysis-state" aria-live="polite"><span className="spinner"></span><strong>{t("candidate.ai.pendingTitle")}</strong><p>{t("candidate.ai.pendingText")}</p></div>}
    {ai.status === "FAILED" && <div className="analysis-state"><Icon name="info" size={20}/><strong>{t("candidate.ai.failedTitle")}</strong><p>{t("candidate.ai.failedText")}</p>{rerunButton}</div>}

    {ai.status === "DONE" && ai.score != null && <>
      <div className="match-score-block">
        <div className={`large-score ${scoreTone(ai.score)}`}>
          <svg viewBox="0 0 120 120"><circle cx="60" cy="60" r="52"/><circle className="score-progress" cx="60" cy="60" r="52" strokeDasharray={`${(ai.score / 100) * RING_CIRCUMFERENCE} ${RING_CIRCUMFERENCE}`}/></svg>
          <div><strong>{ai.score}%</strong><span>{t(`candidate.ai.tone.${scoreTone(ai.score)}`)}</span></div>
        </div>
        <p>{ai.yearsOfExperience != null ? t("candidate.ai.experience", { count: ai.yearsOfExperience }) : t("candidate.ai.basis")}</p>
      </div>
      <div className="analysis-section"><div className="analysis-section-title"><strong>{t("candidate.ai.matching")}</strong><span>{t("candidate.ai.ofRequired", { count: ai.matchingSkills.length, total: ai.matchingSkills.length + ai.missingSkills.length })}</span></div>
        <div className="analysis-tags matching">{ai.matchingSkills.map((skill) => <span key={skill}><Icon name="check" size={12}/>{skill}</span>)}{ai.matchingSkills.length === 0 && <small>{t("candidate.ai.none")}</small>}</div></div>
      <div className="analysis-section"><div className="analysis-section-title"><strong>{t("candidate.ai.missing")}</strong><span>{t("candidate.ai.identified", { count: ai.missingSkills.length })}</span></div>
        <div className="analysis-tags missing">{ai.missingSkills.map((skill) => <span key={skill}>{skill}</span>)}{ai.missingSkills.length === 0 && <small>{t("candidate.ai.none")}</small>}</div></div>
      {ai.summary && <div className="ai-summary"><strong><Icon name="sparkles" size={14}/>{t("candidate.ai.summary")}</strong><p>{ai.summary}</p></div>}
      {canRerun && <div className="analysis-rerun">{rerunButton}</div>}
    </>}

    <div className="ai-notice"><Icon name="info" size={17}/><p><strong>{t("candidate.ai.noticeTitle")}</strong>{t("candidate.ai.notice")}</p></div>
  </article>;
}
