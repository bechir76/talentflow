import { useTranslation } from "react-i18next";
import { Link, useNavigate, useParams } from "react-router";
import Icon from "@/components/common/Icon";
import { WorkModeBadge } from "@/features/jobs/components/JobBadges";
import { formatDate, formatSalaryRange } from "@/lib/format";
import { ApiError } from "@/lib/api/client";
import { usePublicJob } from "../api/publicJobs";

export default function CareerJobPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams();
  const job = usePublicJob(id);

  if (job.isPending) return <div className="table-empty"><strong>{t("common.loading")}</strong></div>;
  if (job.isError) {
    const notFound = job.error instanceof ApiError && job.error.status === 404;
    return <div className="table-empty career-missing">
      <Icon name="search" size={24}/><strong>{t(notFound ? "careers.notFound.title" : "common.loadError")}</strong>
      {notFound && <span>{t("careers.notFound.text")}</span>}
      <Link className="secondary-button" to="/careers">{t("careers.backToJobs")}</Link>
    </div>;
  }

  const data = job.data;
  const salary = formatSalaryRange(data.salaryMin, data.salaryMax, data.currency);
  const apply = () => navigate(`/careers/${data.id}/apply`);

  return <div className="career-detail">
    <div className="breadcrumbs"><Link to="/careers">{t("careers.title")}</Link><Icon name="chevron" size={13}/><strong>{data.title}</strong></div>
    <header className="career-detail-head">
      <div>
        <span className="eyebrow">{data.departmentName}</span>
        <h1>{data.title}</h1>
        <p><Icon name="map" size={14}/>{data.location}<i></i><WorkModeBadge mode={data.workMode}/><i></i>{t("careers.posted", { date: formatDate(data.postedAt) })}</p>
      </div>
      <button className="primary-button" onClick={apply}>{t("careers.applyNow")}<Icon name="arrow"/></button>
    </header>
    <div className="career-detail-grid">
      <article className="panel career-description">
        <h2>{t("careers.aboutRole")}</h2>
        <div className="job-description">{data.description}</div>
      </article>
      <aside className="panel career-facts">
        <h3>{t("careers.facts")}</h3>
        <dl>
          <dt>{t("jobs.form.department")}</dt><dd>{data.departmentName}</dd>
          <dt>{t("jobs.form.location")}</dt><dd>{data.location}</dd>
          <dt>{t("jobs.form.workMode")}</dt><dd>{t(`jobs.workMode.${data.workMode}`)}</dd>
          <dt>{t("careers.salary")}</dt><dd>{salary ?? t("careers.salaryNotGiven")}</dd>
        </dl>
        <h3>{t("jobs.form.skills")}</h3>
        <div className="analysis-tags matching">{data.requiredSkills.map((skill) => <span key={skill}><Icon name="check" size={12}/>{skill}</span>)}</div>
        <button className="primary-button career-apply" onClick={apply}>{t("careers.applyNow")}<Icon name="arrow"/></button>
        <p className="career-privacy"><Icon name="shield" size={14}/>{t("careers.privacyNote")}</p>
      </aside>
    </div>
  </div>;
}
