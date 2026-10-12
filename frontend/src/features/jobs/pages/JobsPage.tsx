import { useState } from "react";
import { useNavigate } from "react-router";
import Icon from "@/components/common/Icon";
import { jobRows } from "../mock";

export default function JobsPage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("All statuses");
  const visibleJobs = jobRows.filter(job =>
    job.title.toLowerCase().includes(query.toLowerCase()) && (status === "All statuses" || job.status === status)
  );
  return <div className="page jobs-page">
    <div className="page-heading jobs-heading"><div><span className="eyebrow">Recruitment</span><h1>Jobs</h1><p>Manage job postings and track applicants across your hiring team.</p></div><button className="primary-button" onClick={() => navigate("/jobs/new")}><Icon name="plus"/>Create Job</button></div>
    <section className="jobs-summary">
      <div><span className="kpi-icon indigo"><Icon name="briefcase"/></span><p><strong>12</strong><span>Open jobs</span></p></div>
      <div><span className="kpi-icon blue"><Icon name="users"/></span><p><strong>148</strong><span>Active applicants</span></p></div>
      <div><span className="kpi-icon amber"><Icon name="file"/></span><p><strong>3</strong><span>Drafts</span></p></div>
      <div><span className="kpi-icon green"><Icon name="check"/></span><p><strong>8</strong><span>Hired this quarter</span></p></div>
    </section>
    <section className="jobs-table-panel panel">
      <div className="table-toolbar">
        <div className="table-search"><Icon name="search"/><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search jobs..."/></div>
        <select value={status} onChange={(e) => setStatus(e.target.value)}><option>All statuses</option><option>Published</option><option>Draft</option><option>Closed</option></select>
        <button className="filter-button"><Icon name="filter"/>Department<Icon name="chevron" size={13}/></button>
        <button className="filter-button"><Icon name="map"/>Location<Icon name="chevron" size={13}/></button>
        <span>{visibleJobs.length} jobs</span>
      </div>
      <div className="jobs-table-wrap">
        <table className="jobs-table">
          <thead><tr><th>Title</th><th>Department</th><th>Location</th><th>Work Mode</th><th>Applicants</th><th>Status</th><th>Created Date</th><th><span className="sr-only">Actions</span></th></tr></thead>
          <tbody>{visibleJobs.map((job) => <tr key={job.title}>
            <td><button className="job-link" onClick={() => job.title === "Senior Java Developer" ? navigate("/pipeline") : undefined}><span className="job-table-icon"><Icon name="briefcase" size={16}/></span><span><strong>{job.title}</strong><small>{job.status === "Draft" ? "Not visible to candidates" : "careers.talentflow.eu"}</small></span></button></td>
            <td>{job.department}</td><td>{job.location}</td>
            <td><span className={`work-badge ${job.mode.toLowerCase().replace("-", "")}`}><i></i>{job.mode}</span></td>
            <td><button className="applicant-count" onClick={() => navigate("/pipeline")}><span>{job.applicants}</span>{job.applicants === 1 ? "applicant" : "applicants"}</button></td>
            <td><span className={`job-status ${job.status.toLowerCase()}`}><i></i>{job.status}</span></td>
            <td>{job.created}</td><td><button className="more-button"><Icon name="more"/></button></td>
          </tr>)}</tbody>
        </table>
        {visibleJobs.length === 0 && <div className="table-empty"><Icon name="search" size={24}/><strong>No jobs found</strong><span>Try changing your search or filters.</span></div>}
      </div>
      <div className="table-footer"><span>Showing 1–{visibleJobs.length} of {visibleJobs.length} jobs</span><div><button disabled>Previous</button><button className="active">1</button><button>2</button><button>Next</button></div></div>
    </section>
  </div>;
}
