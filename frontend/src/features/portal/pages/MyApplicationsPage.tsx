import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";
import Icon from "@/components/common/Icon";
import Pagination from "@/components/common/Pagination";
import { STAGES, useMyApplications, type ApplicationStage, type MyApplication } from "@/features/applications/api/applications";
import { WorkModeBadge } from "@/features/jobs/components/JobBadges";
import { formatDate } from "@/lib/format";

function StageStepper({ stage }: { stage: ApplicationStage }) {
  const { t } = useTranslation();
  const rejected = stage === "REJECTED";
  const current = rejected ? -1 : STAGES.indexOf(stage);
  return <ol className={`stage-stepper ${rejected ? "rejected" : ""}`} aria-label={t("portal.stageLabel")}>
    {STAGES.map((step, index) => <li key={step} className={index < current ? "done" : index === current ? "current" : ""} aria-current={index === current ? "step" : undefined}>
      <span>{index < current ? <Icon name="check" size={11}/> : index + 1}</span>
      <small>{t(`portal.stages.${step}`)}</small>
    </li>)}
  </ol>;
}

function ApplicationCard({ application }: { application: MyApplication }) {
  const { t } = useTranslation();
  const title = application.jobTitle ?? t("portal.jobRemoved");
  return <article className="panel application-card">
    <div className="application-card-head">
      <span className="job-table-icon"><Icon name="briefcase" size={16}/></span>
      <div>
        {application.jobOpen ? <Link to={`/careers/${application.jobId}`}><strong>{title}</strong></Link> : <strong>{title}</strong>}
        <p>{application.jobLocation && <><Icon name="map" size={12}/>{application.jobLocation}</>}{application.workMode && <WorkModeBadge mode={application.workMode}/>}</p>
      </div>
      <small>{t("portal.appliedOn", { date: formatDate(application.appliedAt) })}</small>
    </div>
    {application.stage === "REJECTED"
      ? <p className="application-closed"><Icon name="info" size={14}/>{t("portal.rejected")}</p>
      : <StageStepper stage={application.stage}/>}
  </article>;
}

/** Candidate home: every application with its current stage. */
export default function MyApplicationsPage() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const applications = useMyApplications(page);
  const data = applications.data;

  return <div className="page portal-page">
    <div className="page-heading"><div><span className="eyebrow">{t("portal.eyebrow")}</span><h1>{t("portal.title")}</h1><p>{t("portal.subtitle")}</p></div><Link className="primary-button" to="/careers"><Icon name="search"/>{t("nav.findJobs")}</Link></div>
    {applications.isPending && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
    {applications.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong></div>}
    {data?.totalElements === 0 && <section className="panel">
      <div className="table-empty"><Icon name="file" size={24}/><strong>{t("portal.emptyTitle")}</strong><span>{t("portal.emptyText")}</span></div>
    </section>}
    <div className="application-list">{data?.content.map((application) => <ApplicationCard key={application.id} application={application}/>)}</div>
    {data && data.totalPages > 1 && <div className="panel careers-pagination"><Pagination {...data} onPageChange={setPage}/></div>}
  </div>;
}
