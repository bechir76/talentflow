import { useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import ApplicationsChart from "../components/ApplicationsChart";
import FunnelChart from "../components/FunnelChart";
import KpiCard from "../components/KpiCard";
import { todaysInterviews, topMatches } from "../mock";

export default function DashboardPage() {
  const navigate = useNavigate();
  const user = useCurrentUser();
  const canCreateJobs = user.roles.includes("RECRUITER") || user.roles.includes("ADMIN");
  return <div className="page dashboard">
    <div className="page-heading"><div><span className="eyebrow">Tuesday, 18 February</span><h1>Good morning, {user.firstName ?? user.fullName}</h1><p>Here's what's happening with your hiring pipeline today.</p></div>{canCreateJobs && <button className="primary-button" onClick={() => navigate("/jobs/new")}><Icon name="plus"/>Create job</button>}</div>
    <section className="kpi-grid">
      <KpiCard icon="briefcase" label="Open Jobs" value="12" trend="8.2%" note="vs. last month" color="indigo"/>
      <KpiCard icon="users" label="Active Candidates" value="148" trend="12.4%" note="vs. last month" color="blue"/>
      <KpiCard icon="calendar" label="Interviews This Week" value="9" note="3 scheduled today" color="amber"/>
      <KpiCard icon="clock" label="Avg. Time to Hire" value="21 days" trend="3 days faster" note="than last quarter" color="green"/>
    </section>
    <section className="analytics-grid"><ApplicationsChart/><FunnelChart/></section>
    <section className="lists-grid">
      <article className="panel matches-panel">
        <div className="panel-head"><div><div className="title-with-icon"><span className="sparkle-icon"><Icon name="sparkles" size={16}/></span><h3>Top AI Matches Today</h3></div><p>Best new candidates based on job requirements</p></div><button className="link-button" onClick={() => navigate("/pipeline")}>View all <Icon name="arrow" size={15}/></button></div>
        <div className="match-list">{topMatches.map((candidate, i) => <div className="match-row" key={candidate.name}>
          <Avatar initials={candidate.initials} color={candidate.color}/><div className="match-info"><strong>{candidate.name}</strong><span>{candidate.role}</span></div>
          <div className="match-job"><span>Matched for</span><strong>Senior Java Developer</strong></div>
          <span className={`score-pill ${i === 2 ? "good" : ""}`}><Icon name="sparkles" size={13}/>{candidate.score}% match</span>
          <button className="secondary-button" onClick={() => navigate("/pipeline")}>View</button>
        </div>)}</div>
      </article>
      <article className="panel interviews-panel">
        <div className="panel-head"><div><h3>Upcoming Interviews</h3><p>Your schedule this week</p></div><button className="icon-button"><Icon name="calendar"/></button></div>
        <div className="date-heading"><span>Today</span><b>3 interviews</b></div>
        {todaysInterviews.map(([time, name, type, interviewer], i) => <div className="interview-row" key={name}>
          <div className="time"><strong>{time}</strong><span>{i === 0 ? "60 min" : "45 min"}</span></div><i className={`timeline-dot dot-${i}`}></i>
          <Avatar initials={name.split(" ").map(x => x[0]).join("")} color={i === 0 ? "rose" : i === 1 ? "violet" : "blue"} small/>
          <div className="interview-info"><strong>{name}</strong><span>{type}</span></div><Avatar initials={interviewer} color="gray" small/>
        </div>)}
        <button className="full-link">Open calendar <Icon name="arrow" size={15}/></button>
      </article>
    </section>
  </div>;
}
