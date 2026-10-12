import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router";
import Icon from "@/components/common/Icon";
import Pagination from "@/components/common/Pagination";
import RowMenu, { type RowMenuItem } from "@/components/common/RowMenu";
import { formatDate } from "@/lib/format";
import { useDebouncedValue } from "@/lib/useDebouncedValue";
import { useChangeJobStatus, useDepartments, useJobs, useJobStats, type JobStatus, type JobSummary } from "../api/jobs";
import { JobStatusBadge, WorkModeBadge } from "../components/JobBadges";

const STATUSES: JobStatus[] = ["PUBLISHED", "DRAFT", "CLOSED"];

export default function JobsPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const status = (params.get("status") as JobStatus | null) ?? undefined;
  const departmentId = params.get("departmentId") ?? undefined;
  const page = Number(params.get("page") ?? 0);
  const [query, setQuery] = useState(params.get("q") ?? "");
  const debouncedQuery = useDebouncedValue(query.trim());

  const updateParams = (changes: Record<string, string | undefined>) => {
    const next = new URLSearchParams(params);
    for (const [key, value] of Object.entries(changes)) {
      if (value) next.set(key, value); else next.delete(key);
    }
    setParams(next, { replace: true });
  };

  useEffect(() => {
    if (debouncedQuery !== (params.get("q") ?? "")) updateParams({ q: debouncedQuery || undefined, page: undefined });
    // Only react to the debounced text; params changes are written by this page itself.
  }, [debouncedQuery]);

  const jobs = useJobs({ q: debouncedQuery || undefined, status, departmentId, page });
  const stats = useJobStats();
  const departments = useDepartments();
  const changeStatus = useChangeJobStatus();

  const menuItems = (job: JobSummary): RowMenuItem[] => {
    const items: RowMenuItem[] = [
      { label: t("jobs.actions.edit"), onSelect: () => navigate(`/jobs/${job.id}/edit`) },
      { label: t("jobs.actions.viewPipeline"), onSelect: () => navigate(`/pipeline?jobId=${job.id}`) },
    ];
    if (job.status === "PUBLISHED") {
      items.push({ label: t("jobs.actions.viewOnCareers"), onSelect: () => window.open(`/careers/${job.id}`, "_blank", "noopener") });
      items.push({ label: t("jobs.actions.close"), danger: true, onSelect: () => changeStatus.mutate({ id: job.id, status: "CLOSED" }) });
    } else {
      items.push({ label: t(job.status === "DRAFT" ? "jobs.actions.publish" : "jobs.actions.reopen"), onSelect: () => changeStatus.mutate({ id: job.id, status: "PUBLISHED" }) });
    }
    return items;
  };

  const data = jobs.data;
  const statValue = (value: number | undefined) => value ?? "–";

  return <div className="page jobs-page">
    <div className="page-heading jobs-heading"><div><span className="eyebrow">{t("jobs.eyebrow")}</span><h1>{t("jobs.title")}</h1><p>{t("jobs.subtitle")}</p></div><button className="primary-button" onClick={() => navigate("/jobs/new")}><Icon name="plus"/>{t("jobs.create")}</button></div>
    <section className="jobs-summary">
      <div><span className="kpi-icon indigo"><Icon name="briefcase"/></span><p><strong>{statValue(stats.data?.published)}</strong><span>{t("jobs.stats.open")}</span></p></div>
      <div><span className="kpi-icon amber"><Icon name="file"/></span><p><strong>{statValue(stats.data?.drafts)}</strong><span>{t("jobs.stats.drafts")}</span></p></div>
      <div><span className="kpi-icon blue"><Icon name="users"/></span><p><strong>{statValue(stats.data?.activeApplicants)}</strong><span>{t("jobs.stats.activeApplicants")}</span></p></div>
      <div><span className="kpi-icon green"><Icon name="lock"/></span><p><strong>{statValue(stats.data?.closed)}</strong><span>{t("jobs.stats.closed")}</span></p></div>
    </section>
    <section className="jobs-table-panel panel">
      <div className="table-toolbar">
        <div className="table-search"><Icon name="search"/><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder={t("jobs.searchPlaceholder")} aria-label={t("jobs.searchPlaceholder")}/></div>
        <select value={status ?? ""} onChange={(e) => updateParams({ status: e.target.value || undefined, page: undefined })} aria-label={t("jobs.filters.status")}>
          <option value="">{t("jobs.filters.allStatuses")}</option>
          {STATUSES.map((s) => <option key={s} value={s}>{t(`jobs.status.${s}`)}</option>)}
        </select>
        <select value={departmentId ?? ""} onChange={(e) => updateParams({ departmentId: e.target.value || undefined, page: undefined })} aria-label={t("jobs.filters.department")}>
          <option value="">{t("jobs.filters.allDepartments")}</option>
          {departments.data?.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        {data && <span>{t("jobs.count", { count: data.totalElements })}</span>}
      </div>
      <div className="jobs-table-wrap">
        <table className="jobs-table">
          <thead><tr><th>{t("jobs.columns.title")}</th><th>{t("jobs.columns.department")}</th><th>{t("jobs.columns.location")}</th><th>{t("jobs.columns.workMode")}</th><th>{t("jobs.columns.applicants")}</th><th>{t("jobs.columns.status")}</th><th>{t("jobs.columns.created")}</th><th><span className="sr-only">{t("common.actions")}</span></th></tr></thead>
          <tbody>{data?.content.map((job) => <tr key={job.id}>
            <td><button className="job-link" onClick={() => navigate(`/jobs/${job.id}/edit`)}><span className="job-table-icon"><Icon name="briefcase" size={16}/></span><span><strong>{job.title}</strong><small>{t(job.status === "PUBLISHED" ? "jobs.visibleOnCareers" : "jobs.notVisible")}</small></span></button></td>
            <td>{job.departmentName}</td><td>{job.location}</td>
            <td><WorkModeBadge mode={job.workMode}/></td>
            <td><button className="applicant-count" onClick={() => navigate(`/pipeline?jobId=${job.id}`)}><span>{job.applicantCount}</span>{t("jobs.applicants", { count: job.applicantCount })}</button></td>
            <td><JobStatusBadge status={job.status}/></td>
            <td>{formatDate(job.createdAt)}</td>
            <td><RowMenu items={menuItems(job)}/></td>
          </tr>)}</tbody>
        </table>
        {jobs.isPending && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
        {jobs.isError && <div className="table-empty"><Icon name="info" size={24}/><strong>{t("common.loadError")}</strong><span>{jobs.error.message}</span></div>}
        {data?.totalElements === 0 && <div className="table-empty"><Icon name="search" size={24}/><strong>{t("jobs.empty.title")}</strong><span>{t("jobs.empty.text")}</span></div>}
      </div>
      {data && data.totalElements > 0 && <Pagination {...data} onPageChange={(p) => updateParams({ page: p > 0 ? String(p) : undefined })}/>}
    </section>
  </div>;
}
