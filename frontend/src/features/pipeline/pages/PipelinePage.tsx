import { useMemo, useState } from "react";
import { useNavigate } from "react-router";
import Icon from "@/components/common/Icon";
import CandidateCard from "../components/CandidateCard";
import { pipeline } from "../mock";

export default function PipelinePage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const filtered = useMemo(() => pipeline.map(col => ({ ...col, people: col.people.filter(p => p.name.toLowerCase().includes(search.toLowerCase())) })), [search]);
  return <div className="page pipeline-page">
    <div className="breadcrumbs"><span>Jobs</span><Icon name="chevron" size={13}/><span>Engineering</span><Icon name="chevron" size={13}/><strong>Senior Java Developer</strong></div>
    <div className="job-heading">
      <div><div className="job-title-line"><h1>Senior Java Developer</h1><span className="status-badge"><i></i>Active</span></div><p>Berlin, Germany · Hybrid · Full-time <span>€75k–€95k</span></p></div>
      <div className="job-actions"><button className="secondary-button"><Icon name="more"/>More</button><button className="primary-button"><Icon name="plus"/>Add candidate</button></div>
    </div>
    <div className="pipeline-toolbar">
      <div className="pipeline-search"><Icon name="search"/><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search candidates..."/></div>
      <button className="filter-button"><Icon name="sparkles"/>AI score: High to low <Icon name="chevron" size={13}/></button>
      <button className="filter-button"><Icon name="filter"/>Skills <span className="filter-count">3</span><Icon name="chevron" size={13}/></button>
      <button className="filter-button"><Icon name="calendar"/>Date added <Icon name="chevron" size={13}/></button>
      <span className="toolbar-spacer"></span><span className="candidate-total">10 candidates</span>
    </div>
    <div className="kanban">
      {filtered.map((column) => <section className="kanban-column" key={column.title}>
        <div className="column-head"><span className={`stage-dot ${column.tone}`}></span><strong>{column.title}</strong><b>{column.people.length}</b><button className="more-button"><Icon name="more"/></button></div>
        <div className="column-cards">{column.people.map(person => <CandidateCard key={person.id} person={person} onOpen={() => navigate(`/candidates/${person.id}`)}/>)}
          {column.people.length === 0 && <div className="empty-column">No matching candidates</div>}
        </div>
      </section>)}
    </div>
  </div>;
}
