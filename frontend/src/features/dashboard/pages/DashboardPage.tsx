import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { scoreTone } from "@/features/candidates/score";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { initialsOf } from "@/lib/format";
import { currentLanguage } from "@/lib/i18n";
import { useDashboard } from "../api/dashboard";
import ApplicationsChart from "../components/ApplicationsChart";
import FunnelChart from "../components/FunnelChart";
import KpiCard from "../components/KpiCard";

const AVATAR_COLORS = ["violet", "rose", "blue"];

function greetingKey(hour: number): "morning" | "afternoon" | "evening" {
  if (hour < 12) return "morning";
  if (hour < 18) return "afternoon";
  return "evening";
}

export default function DashboardPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const user = useCurrentUser();
  const dashboard = useDashboard();
  const canCreateJobs = user.roles.includes("RECRUITER") || user.roles.includes("ADMIN");
  const isManager = !canCreateJobs;
  const now = new Date();
  const today = new Intl.DateTimeFormat(currentLanguage() === "de" ? "de-DE" : "en-GB", { weekday: "long", day: "numeric", month: "long" }).format(now);
  const data = dashboard.data;
  const daysToHire = data?.averageDaysToHire;

  return <div className="page dashboard">
    <div className="page-heading"><div><span className="eyebrow">{today}</span><h1>{t(`dashboard.greeting.${greetingKey(now.getHours())}`, { name: user.firstName ?? user.fullName })}</h1><p>{t("dashboard.subtitle")}</p></div>{canCreateJobs && <button className="primary-button" onClick={() => navigate("/jobs/new")}><Icon name="plus"/>{t("jobs.create")}</button>}</div>
    {dashboard.isError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{t("common.loadError")}</div>}
    <section className="kpi-grid">
      <KpiCard icon="briefcase" label={t("dashboard.kpi.openJobs")} value={data ? String(data.openJobs) : "–"} note={t("dashboard.kpi.openJobsNote")} color="indigo"/>
      <KpiCard icon="users" label={t("dashboard.kpi.activeCandidates")} value={data ? String(data.activeCandidates) : "–"} note={t("dashboard.kpi.activeCandidatesNote")} color="blue"/>
      <KpiCard icon="calendar" label={t("dashboard.kpi.interviews")} value={data ? String(data.interviewsThisWeek) : "–"} note={t(isManager ? "dashboard.kpi.interviewsNoteOwn" : "dashboard.kpi.interviewsNote")} color="amber"/>
      <KpiCard icon="clock" label={t("dashboard.kpi.timeToHire")} value={daysToHire != null ? t("dashboard.kpi.days", { count: Math.round(daysToHire) }) : "–"} note={t("dashboard.kpi.timeToHireNote")} color="green"/>
    </section>
    {data && <section className="analytics-grid"><ApplicationsChart data={data.applicationsPerJob}/><FunnelChart funnel={data.funnel}/></section>}
    <section className={`lists-grid ${isManager ? "single" : ""}`}>
      {!isManager && <article className="panel matches-panel">
        <div className="panel-head"><div><div className="title-with-icon"><span className="sparkle-icon"><Icon name="sparkles" size={16}/></span><h3>{t("dashboard.topMatches")}</h3></div><p>{t("dashboard.topMatchesSubtitle")}</p></div><button className="link-button" onClick={() => navigate("/pipeline")}>{t("dashboard.viewAll")} <Icon name="arrow" size={15}/></button></div>
        <div className="match-list">
          {data?.topMatches.map((match, i) => <div className="match-row" key={match.applicationId}>
            <Avatar initials={initialsOf(match.candidateName)} color={AVATAR_COLORS[i % AVATAR_COLORS.length]}/><div className="match-info"><strong>{match.candidateName}</strong></div>
            <div className="match-job"><span>{t("dashboard.matchedFor")}</span><strong>{match.jobTitle}</strong></div>
            <span className={`score-pill ${scoreTone(match.score) === "excellent" ? "" : "good"}`}><Icon name="sparkles" size={13}/>{t("dashboard.match", { score: match.score })}</span>
            <button className="secondary-button" onClick={() => navigate(`/applications/${match.applicationId}`)}>{t("dashboard.view")}</button>
          </div>)}
          {data?.topMatches.length === 0 && <div className="table-empty list-empty"><strong>{t("dashboard.noMatches")}</strong><span>{t("dashboard.noMatchesText")}</span></div>}
        </div>
        <p className="ai-footnote"><Icon name="info" size={12}/>{t("candidate.ai.notice")}</p>
      </article>}
      <article className="panel interviews-panel">
        <div className="panel-head"><div><h3>{t("dashboard.interviewsTitle")}</h3><p>{t("dashboard.interviewsSubtitle")}</p></div><button className="icon-button" onClick={() => navigate("/interviews")} aria-label={t("nav.interviews")}><Icon name="calendar"/></button></div>
        {data?.upcomingInterviews.map((interview, i) => {
          const start = new Date(interview.scheduledAt);
          return <button className="interview-row" key={interview.interviewId} onClick={() => navigate(`/applications/${interview.applicationId}`)}>
            <div className="time"><strong>{start.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" })}</strong><span>{start.toLocaleDateString(undefined, { weekday: "short", day: "numeric" })}</span></div><i className="timeline-dot"></i>
            <Avatar initials={initialsOf(interview.candidateName ?? "?")} color={AVATAR_COLORS[i % AVATAR_COLORS.length]} small/>
            <div className="interview-info"><strong>{interview.candidateName}</strong><span>{interview.jobTitle} · {t("interviews.minutes", { count: interview.durationMinutes })}</span></div>
            <Avatar initials={initialsOf(interview.interviewerNames[0] ?? "?")} color="gray" small/>
          </button>;
        })}
        {data?.upcomingInterviews.length === 0 && <div className="table-empty list-empty"><Icon name="calendar" size={22}/><strong>{t("dashboard.noInterviews")}</strong><span>{t("dashboard.noInterviewsText")}</span></div>}
        <button className="full-link" onClick={() => navigate("/interviews")}>{t("dashboard.openCalendar")} <Icon name="arrow" size={15}/></button>
      </article>
    </section>
  </div>;
}
