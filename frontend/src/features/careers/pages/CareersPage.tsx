import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";
import Icon from "@/components/common/Icon";
import Pagination from "@/components/common/Pagination";
import type { WorkMode } from "@/features/jobs/api/jobs";
import { WorkModeBadge } from "@/features/jobs/components/JobBadges";
import { formatSalaryRange } from "@/lib/format";
import { useDebouncedValue } from "@/lib/useDebouncedValue";
import { usePublicJobs } from "../api/publicJobs";

const WORK_MODES: WorkMode[] = ["REMOTE", "HYBRID", "ONSITE"];
const MAX_SKILLS_ON_CARD = 4;

export default function CareersPage() {
  const { t } = useTranslation();
  const [query, setQuery] = useState("");
  const [workMode, setWorkMode] = useState<WorkMode>();
  const [page, setPage] = useState(0);
  const q = useDebouncedValue(query.trim());
  const jobs = usePublicJobs({ q: q || undefined, workMode, page });

  return <>
    <section className="careers-hero">
      <span className="eyebrow light"><Icon name="sparkles" size={15}/> {t("careers.eyebrow")}</span>
      <h1>{t("careers.title")}</h1>
      <p>{t("careers.subtitle")}</p>
      <div className="careers-search">
        <Icon name="search"/>
        <input value={query} onChange={(e) => { setQuery(e.target.value); setPage(0); }} placeholder={t("careers.searchPlaceholder")} aria-label={t("careers.searchPlaceholder")}/>
        <select value={workMode ?? ""} onChange={(e) => { setWorkMode((e.target.value || undefined) as WorkMode | undefined); setPage(0); }} aria-label={t("jobs.form.workMode")}>
          <option value="">{t("careers.allWorkModes")}</option>
          {WORK_MODES.map((mode) => <option key={mode} value={mode}>{t(`jobs.workMode.${mode}`)}</option>)}
        </select>
      </div>
    </section>
    <section className="careers-list">
      {jobs.data && <p className="careers-count">{t("careers.count", { count: jobs.data.totalElements })}</p>}
      <div className="career-grid">
        {jobs.data?.content.map((job) => {
          const salary = formatSalaryRange(job.salaryMin, job.salaryMax, job.currency);
          return <Link key={job.id} to={`/careers/${job.id}`} className="career-card panel">
            <span className="job-table-icon"><Icon name="briefcase" size={16}/></span>
            <div className="career-card-body">
              <small>{job.departmentName}</small>
              <h2>{job.title}</h2>
              <p><Icon name="map" size={13}/>{job.location}</p>
              <div className="skills">{job.requiredSkills.slice(0, MAX_SKILLS_ON_CARD).map((skill) => <span key={skill}>{skill}</span>)}</div>
              <div className="career-card-foot"><WorkModeBadge mode={job.workMode}/>{salary && <b>{salary}</b>}<Icon name="arrow" size={15}/></div>
            </div>
          </Link>;
        })}
      </div>
      {jobs.isPending && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
      {jobs.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong></div>}
      {jobs.data?.totalElements === 0 && <div className="table-empty"><Icon name="search" size={24}/><strong>{t("careers.empty.title")}</strong><span>{t("careers.empty.text")}</span></div>}
      {jobs.data && jobs.data.totalPages > 1 && <div className="panel careers-pagination"><Pagination {...jobs.data} onPageChange={setPage}/></div>}
    </section>
  </>;
}
