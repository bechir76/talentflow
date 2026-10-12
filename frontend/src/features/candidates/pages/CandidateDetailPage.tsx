import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { formatDate } from "@/lib/format";
import { useApplication, type ApplicationDetail } from "../api/applications";
import AiAnalysis from "../components/AiAnalysis";
import CandidateTabs, { type TimelineEntry } from "../components/CandidateTabs";
import CvPreview from "../components/CvPreview";

export function initialsOf(name: string): string {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]!.toUpperCase()).join("");
}

function buildTimeline(application: ApplicationDetail, t: (key: string, options?: Record<string, unknown>) => string): TimelineEntry[] {
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
  return entries.sort((a, b) => b.at.localeCompare(a.at));
}

export default function CandidateDetailPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams();
  const user = useCurrentUser();
  const query = useApplication(id);
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
      <div className="candidate-actions"><span className={`stage-pill stage-${application.stage.toLowerCase()}`}>{t(`pipeline.stages.${application.stage}`)}</span></div>
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
    <CandidateTabs timeline={buildTimeline(application, t as never)} formatAt={formatDateTime}/>
  </div>;
}
