import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import InterviewList from "@/features/interviews/components/InterviewList";
import ScheduleInterviewModal from "@/features/interviews/components/ScheduleInterviewModal";
import { useStageHistory, type StageChange } from "@/features/pipeline/api/pipeline";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { formatDate, initialsOf } from "@/lib/format";
import { useApplication, type ApplicationDetail } from "../api/applications";
import AiAnalysis from "../components/AiAnalysis";
import CandidateTabs, { type TimelineEntry } from "../components/CandidateTabs";
import CvPreview from "../components/CvPreview";
import StageControls from "../components/StageControls";

function buildTimeline(application: ApplicationDetail, history: StageChange[], t: (key: string, options?: Record<string, unknown>) => string): TimelineEntry[] {
  const entries: TimelineEntry[] = [
    { id: "applied", icon: "file", title: t("candidate.timeline.applied"), detail: t("candidate.timeline.appliedDetail"), by: application.candidate.fullName, at: application.appliedAt },
  ];
  if (application.ai.analyzedAt && application.ai.status !== "PENDING") {
    entries.push({
      id: "ai", icon: "sparkles", at: application.ai.analyzedAt,
      title: t(application.ai.status === "DONE" ? "candidate.timeline.aiDone" : "candidate.timeline.aiFailed"),
      detail: application.ai.status === "DONE" ? t("candidate.timeline.aiDetail", { score: application.ai.score }) : undefined,
    });
  }
  for (const change of history) {
    const from = t(`pipeline.stages.${change.fromStage}`);
    const to = t(`pipeline.stages.${change.toStage}`);
    entries.push({
      id: change.id, icon: change.toStage === "REJECTED" ? "info" : "arrow", at: change.changedAt, by: change.changedByName ?? undefined,
      title: t("candidate.timeline.moved", { stage: to }),
      detail: [t("candidate.timeline.movedDetail", { from, to }), change.note].filter(Boolean).join(" · "),
    });
  }
  return entries.sort((a, b) => b.at.localeCompare(a.at));
}

export default function CandidateDetailPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams();
  const user = useCurrentUser();
  const query = useApplication(id);
  const history = useStageHistory(id ?? "");
  const [scheduling, setScheduling] = useState(false);
  const canManage = user.roles.includes("RECRUITER") || user.roles.includes("ADMIN");

  if (query.isPending) return <div className="page candidate-page"><div className="table-empty"><strong>{t("common.loading")}</strong></div></div>;
  if (query.isError) return <div className="page candidate-page"><div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong><span>{query.error.message}</span></div></div>;

  const application = query.data;
  const { candidate, job } = application;
  const formatDateTime = (iso: string) => new Date(iso).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });

  return <div className="page candidate-page">
    <div className="breadcrumbs"><button onClick={() => navigate(`/pipeline?jobId=${job.id}`)}>{t("nav.candidates")}</button><Icon name="chevron" size={13}/><span>{job.title}</span><Icon name="chevron" size={13}/><strong>{candidate.fullName}</strong></div>
    <header className="candidate-header">
      <div className="candidate-identity"><Avatar initials={initialsOf(candidate.fullName)} color="rose"/><div>
        <div><h1>{candidate.fullName}</h1>{application.consent && <span className="gdpr-mini"><Icon name="shield" size={12}/>{t("candidate.consentVerified")}</span>}</div>
        <p><Icon name="mail" size={13}/>{candidate.email}{candidate.location && <><i></i><Icon name="map" size={13}/>{candidate.location}</>}{candidate.phone && <><i></i>{candidate.phone}</>}</p>
      </div></div>
      <StageControls applicationId={application.id} jobId={job.id} stage={application.stage} canManage={canManage}>
        {canManage && application.stage !== "HIRED" && application.stage !== "REJECTED" && <button className="secondary-button" onClick={() => setScheduling(true)}><Icon name="calendar"/>{t("interviews.scheduleButton")}</button>}
      </StageControls>
    </header>
    <div className="candidate-meta">
      <span>{t("candidate.appliedFor")} <strong>{job.title} – {job.location}</strong></span>
      <span>{t("candidate.appliedOn", { date: formatDate(application.appliedAt) })}</span>
      {application.consent && <span className="gdpr-retention"><Icon name="shield" size={14}/>{t("candidate.consentGiven", { date: formatDate(application.consent.givenAt), version: application.consent.policyVersion })}</span>}
    </div>
    <div className="candidate-detail-grid">
      <CvPreview applicationId={application.id} available={application.cvAvailable} fileName={`${candidate.fullName.replace(/\s+/g, "_")}_CV.pdf`}/>
      <div className="candidate-side">
        <AiAnalysis applicationId={application.id} ai={application.ai} canRerun={canManage}/>
        {application.coverLetter && <article className="panel cover-letter"><h3>{t("candidate.coverLetter")}</h3><p>{application.coverLetter}</p></article>}
      </div>
    </div>
    <CandidateTabs timeline={buildTimeline(application, history.data ?? [], t as never)} formatAt={formatDateTime} interviews={<InterviewList applicationId={application.id} canManage={canManage}/>}/>
    <ScheduleInterviewModal applicationId={application.id} open={scheduling} onClose={() => setScheduling(false)}/>
  </div>;
}
