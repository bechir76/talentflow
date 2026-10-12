import { useMemo, useState, type ReactNode } from "react";

type Screen = "login" | "dashboard" | "jobs" | "create-job" | "pipeline" | "candidate";
type IconName =
  | "grid" | "briefcase" | "users" | "calendar" | "chart" | "settings"
  | "search" | "bell" | "moon" | "sun" | "chevron" | "menu" | "plus"
  | "sparkles" | "arrow" | "clock" | "filter" | "more" | "shield"
  | "check" | "lock" | "mail" | "eye" | "logout" | "globe"
  | "info" | "file" | "download" | "star" | "map";

function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
  const paths: Record<IconName, ReactNode> = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/></>,
    briefcase: <><rect x="3" y="7" width="18" height="13" rx="3"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M3 12h18M10 12v2h4v-2"/></>,
    users: <><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></>,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M16 3v4M8 3v4M3 10h18"/><path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01"/></>,
    chart: <><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.09A1.7 1.7 0 0 0 8.94 19.4a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.57 15 1.7 1.7 0 0 0 3 14H3v-4h.09A1.7 1.7 0 0 0 4.6 8.94a1.7 1.7 0 0 0-.34-1.88L4.2 7l2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.57 1.7 1.7 0 0 0 10 3V3h4v.09a1.7 1.7 0 0 0 1.06 1.51 1.7 1.7 0 0 0 1.88-.34L17 4.2 19.83 7l-.06.06A1.7 1.7 0 0 0 19.43 9 1.7 1.7 0 0 0 21 10h.09v4H21a1.7 1.7 0 0 0-1.6 1Z"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></>,
    moon: <path d="M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8Z"/>,
    sun: <><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"/></>,
    chevron: <path d="m9 18 6-6-6-6"/>,
    menu: <><path d="M4 6h16M4 12h16M4 18h16"/></>,
    plus: <path d="M12 5v14M5 12h14"/>,
    sparkles: <><path d="m12 3-1.1 3.1L8 7.2l2.9 1.1L12 11.5l1.1-3.2L16 7.2l-2.9-1.1L12 3ZM5.5 13l-.8 2.2-2.2.8 2.2.8.8 2.2.8-2.2 2.2-.8-2.2-.8-.8-2.2ZM18.5 13l-.8 2.2-2.2.8 2.2.8.8 2.2.8-2.2 2.2-.8-2.2-.8-.8-2.2Z"/></>,
    arrow: <><path d="M5 12h14M13 6l6 6-6 6"/></>,
    clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
    filter: <path d="M4 5h16l-6 7v5l-4 2v-7L4 5Z"/>,
    more: <><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></>,
    shield: <><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/><path d="m9 12 2 2 4-4"/></>,
    check: <path d="m5 12 4 4L19 6"/>,
    lock: <><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
    mail: <><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/></>,
    eye: <><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="2.5"/></>,
    logout: <><path d="M10 17l5-5-5-5M15 12H3M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/></>,
    globe: <><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/></>,
    info: <><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/></>,
    file: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6M8 13h8M8 17h6"/></>,
    download: <><path d="M12 3v12M7 10l5 5 5-5M5 21h14"/></>,
    star: <path d="m12 2.5 2.9 5.9 6.5.9-4.7 4.6 1.1 6.5-5.8-3.1-5.8 3.1 1.1-6.5-4.7-4.6 6.5-.9L12 2.5Z"/>,
    map: <><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="2.5"/></>,
  };
  return <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>;
}

const candidates = [
  { name: "Lena Schmidt", initials: "LS", role: "Senior Backend Engineer", score: 94, color: "violet" },
  { name: "Amira Haddad", initials: "AH", role: "Java Software Engineer", score: 92, color: "rose" },
  { name: "Jonas Weber", initials: "JW", role: "Cloud Platform Engineer", score: 89, color: "blue" },
];

const pipeline = [
  { title: "Applied", tone: "gray", people: [
    { name: "David Klein", initials: "DK", role: "Java Developer at Zalando", score: 88, days: 2, tags: ["Java", "Spring Boot"] },
    { name: "Sophia Müller", initials: "SM", role: "Backend Engineer at N26", score: 84, days: 3, tags: ["Java", "Docker"] },
    { name: "Lucas Bernard", initials: "LB", role: "Software Engineer at Qonto", score: 79, days: 1, tags: ["Kotlin", "Java"] },
  ]},
  { title: "Screening", tone: "indigo", people: [
    { name: "Amira Haddad", initials: "AH", role: "Java Engineer at Celonis", score: 92, days: 4, tags: ["Java", "Spring Boot", "Docker"] },
    { name: "Felix Wagner", initials: "FW", role: "Senior Developer at SAP", score: 86, days: 5, tags: ["Java", "AWS"] },
  ]},
  { title: "Interview", tone: "amber", people: [
    { name: "Lena Schmidt", initials: "LS", role: "Backend Lead at Delivery Hero", score: 94, days: 2, tags: ["Java", "Kubernetes"] },
    { name: "Matteo Rossi", initials: "MR", role: "Platform Engineer at SumUp", score: 90, days: 3, tags: ["Spring Boot", "Docker"] },
  ]},
  { title: "Offer", tone: "purple", people: [
    { name: "Jonas Weber", initials: "JW", role: "Cloud Engineer at Flix", score: 89, days: 1, tags: ["Java", "AWS"] },
  ]},
  { title: "Hired", tone: "green", people: [
    { name: "Elisa Fischer", initials: "EF", role: "Senior Java Engineer", score: 96, days: 0, tags: ["Java", "Spring Boot"] },
  ]},
  { title: "Rejected", tone: "red", people: [
    { name: "Paul Meier", initials: "PM", role: "Software Developer", score: 61, days: 2, tags: ["Java"] },
  ]},
];

function Logo({ compact = false }: { compact?: boolean }) {
  return <div className="logo"><span className="logo-mark"><span></span><span></span><span></span></span>{!compact && <strong>TalentFlow</strong>}</div>;
}

function Avatar({ initials, color = "blue", small = false }: { initials: string; color?: string; small?: boolean }) {
  return <span className={`avatar avatar-${color} ${small ? "avatar-small" : ""}`}>{initials}</span>;
}

function Login({ onLogin }: { onLogin: () => void }) {
  const [showPassword, setShowPassword] = useState(false);
  return <main className="login-page">
    <section className="login-brand">
      <Logo />
      <div className="login-copy">
        <span className="eyebrow light"><Icon name="sparkles" size={15}/> Built for modern European teams</span>
        <h1>Hire smarter.<br/>Stay compliant.</h1>
        <p>AI-assisted recruiting that helps you find exceptional people while keeping privacy at the heart of every decision.</p>
      </div>
      <div className="login-art" aria-hidden="true">
        <div className="art-orbit orbit-one"></div><div className="art-orbit orbit-two"></div>
        <div className="art-card art-card-main">
          <div className="art-title"><span className="mini-logo">T</span><span>Candidate match</span><b>94%</b></div>
          <div className="art-profile"><Avatar initials="LS" color="rose"/><div><strong>Lena Schmidt</strong><small>Senior Backend Engineer</small></div></div>
          <div className="art-bars"><i></i><i></i><i></i></div>
          <div className="art-tags"><span>Java</span><span>Spring Boot</span><span>Docker</span></div>
        </div>
        <div className="art-card art-card-small"><span className="success-dot"><Icon name="check" size={13}/></span><div><strong>GDPR ready</strong><small>Data processed in EU</small></div></div>
        <div className="art-card art-card-score"><Icon name="sparkles" size={17}/><div><small>AI screening</small><strong>18h saved</strong></div></div>
      </div>
      <p className="brand-foot"><Icon name="shield" size={16}/> GDPR compliant · Hosted in the EU · ISO 27001</p>
    </section>
    <section className="login-form-side">
      <div className="mobile-login-logo"><Logo/></div>
      <div className="login-form-wrap">
        <div className="form-heading"><h2>Welcome back</h2><p>Sign in to continue to your workspace.</p></div>
        <button className="sso-button" onClick={onLogin}><span className="sso-icon">S</span>Sign in with SSO</button>
        <div className="divider"><span>or continue with email</span></div>
        <form onSubmit={(event) => { event.preventDefault(); onLogin(); }}>
          <label>Email address<input type="email" defaultValue="anna.keller@talentflow.eu"/></label>
          <label>Password<div className="password-wrap"><input type={showPassword ? "text" : "password"} defaultValue="talentflow"/><button type="button" className="field-icon" onClick={() => setShowPassword(!showPassword)}><Icon name="eye" size={18}/></button></div></label>
          <div className="form-options"><label className="checkbox-label"><input type="checkbox" defaultChecked/>Keep me signed in</label><button type="button" className="link-button">Forgot password?</button></div>
          <button className="primary-button login-submit">Sign in <Icon name="arrow" size={17}/></button>
        </form>
        <p className="form-help">Having trouble? <button className="link-button">Contact support</button></p>
      </div>
      <div className="login-footer"><span>© 2025 TalentFlow GmbH</span><span>Privacy</span><span>Imprint</span><span>Deutsch</span></div>
    </section>
  </main>;
}

function Sidebar({ screen, setScreen, collapsed, setCollapsed, role, setRole }: {
  screen: Screen; setScreen: (s: Screen) => void; collapsed: boolean; setCollapsed: (v: boolean) => void;
  role: string; setRole: (r: string) => void;
}) {
  const recruiterNav = [
    { icon: "grid" as IconName, label: "Overview", target: "dashboard" as Screen },
    { icon: "briefcase" as IconName, label: "Jobs", target: "jobs" as Screen, badge: "12" },
    { icon: "users" as IconName, label: "Candidates", target: "pipeline" as Screen },
    { icon: "calendar" as IconName, label: "Interviews", target: "dashboard" as Screen, badge: "9" },
    { icon: "chart" as IconName, label: "Reports", target: "dashboard" as Screen },
  ];
  const roleNav: Record<string, typeof recruiterNav> = {
    Recruiter: recruiterNav,
    "Hiring Manager": [
      { icon: "grid", label: "Overview", target: "dashboard" },
      { icon: "briefcase", label: "My Openings", target: "jobs", badge: "4" },
      { icon: "users", label: "Shortlists", target: "pipeline" },
      { icon: "calendar", label: "Interviews", target: "dashboard", badge: "3" },
    ],
    Admin: [
      { icon: "grid", label: "Overview", target: "dashboard" },
      { icon: "users", label: "Team", target: "dashboard" },
      { icon: "briefcase", label: "All Jobs", target: "jobs", badge: "12" },
      { icon: "chart", label: "Analytics", target: "dashboard" },
      { icon: "shield", label: "Audit Log", target: "dashboard" },
    ],
    Candidate: [
      { icon: "grid", label: "My Overview", target: "dashboard" },
      { icon: "briefcase", label: "Find Jobs", target: "jobs" },
      { icon: "chart", label: "Applications", target: "dashboard", badge: "3" },
      { icon: "calendar", label: "My Interviews", target: "dashboard", badge: "1" },
    ],
  };
  const nav = roleNav[role] ?? recruiterNav;
  return <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
    <div className="sidebar-head"><Logo compact={collapsed}/><button className="collapse-button" onClick={() => setCollapsed(!collapsed)}><Icon name="menu"/></button></div>
    <div className="workspace-switch"><span className="workspace-logo">AK</span>{!collapsed && <><div><strong>Acme GmbH</strong><small>Recruiting</small></div><Icon name="chevron" size={14}/></>}</div>
    <nav className="nav-list">
      {!collapsed && <span className="nav-label">Workspace</span>}
      {nav.map((item, index) => <button key={item.label} className={screen === item.target && ((screen === "dashboard" && index === 0) || screen === "jobs" || (screen === "pipeline" && item.label === "Candidates")) ? "active" : ""} onClick={() => setScreen(item.target)}>
        <Icon name={item.icon}/>{!collapsed && <><span>{item.label}</span>{item.badge && <b>{item.badge}</b>}</>}
      </button>)}
    </nav>
    <div className="sidebar-bottom">
      {!collapsed && <span className="nav-label">Manage</span>}
      <button><Icon name="settings"/>{!collapsed && <span>Settings</span>}</button>
      <button><Icon name="shield"/>{!collapsed && <span>Compliance</span>}</button>
      <div className="role-switch">
        <Avatar initials="AK" color="violet" small/>
        {!collapsed && <div><strong>Anna Keller</strong><select value={role} onChange={(e) => setRole(e.target.value)} aria-label="Select role"><option>Recruiter</option><option>Hiring Manager</option><option>Admin</option><option>Candidate</option></select></div>}
      </div>
    </div>
  </aside>;
}

function Topbar({ dark, setDark, setScreen }: { dark: boolean; setDark: (v: boolean) => void; setScreen: (s: Screen) => void }) {
  const [language, setLanguage] = useState("EN");
  return <header className="topbar">
    <div className="global-search"><Icon name="search"/><input placeholder="Search candidates, jobs, interviews..."/><kbd>⌘ K</kbd></div>
    <div className="top-actions">
      <button className="icon-button theme-button" onClick={() => setDark(!dark)} aria-label="Toggle dark mode"><Icon name={dark ? "sun" : "moon"}/></button>
      <button className="language-button" onClick={() => setLanguage(language === "EN" ? "DE" : "EN")}><Icon name="globe" size={16}/>{language}</button>
      <button className="icon-button notification"><Icon name="bell"/><i></i></button>
      <span className="top-divider"></span>
      <button className="profile-button" onClick={() => setScreen("login")}><Avatar initials="AK" color="violet" small/><span><strong>Anna Keller</strong><small>Recruiter</small></span><Icon name="chevron" size={14}/></button>
    </div>
  </header>;
}

function KpiCard({ icon, label, value, trend, note, color }: { icon: IconName; label: string; value: string; trend?: string; note: string; color: string }) {
  return <article className="kpi-card">
    <div className="kpi-top"><span className={`kpi-icon ${color}`}><Icon name={icon}/></span><button className="more-button"><Icon name="more"/></button></div>
    <span className="kpi-label">{label}</span><div className="kpi-value">{value}</div>
    <div className="kpi-foot">{trend && <span className="trend">↗ {trend}</span>}<span>{note}</span></div>
  </article>;
}

function ApplicationsChart() {
  const bars = [
    ["Java Dev.", 86, "112"], ["Product Des.", 62, "81"], ["Sales Mgr.", 47, "64"],
    ["Data Analyst", 72, "93"], ["People Ops", 34, "46"], ["DevOps Eng.", 55, "72"],
  ];
  return <article className="panel chart-panel">
    <div className="panel-head"><div><h3>Applications per Job</h3><p>New applications in the last 30 days</p></div><button className="select-button">Last 30 days <Icon name="chevron" size={13}/></button></div>
    <div className="bar-chart">
      <div className="y-axis"><span>120</span><span>90</span><span>60</span><span>30</span><span>0</span></div>
      <div className="bars">
        {bars.map(([name, height, count]) => <div className="bar-column" key={name}><div className="bar-value">{count}</div><div className="bar-track"><i style={{ height: `${height}%` }}></i></div><span>{name}</span></div>)}
      </div>
    </div>
  </article>;
}

function FunnelChart() {
  const steps = [
    ["Applied", "428", "100%", "funnel-1"], ["Screening", "196", "46%", "funnel-2"],
    ["Interview", "84", "43%", "funnel-3"], ["Offer", "31", "37%", "funnel-4"], ["Hired", "18", "58%", "funnel-5"],
  ];
  return <article className="panel funnel-panel">
    <div className="panel-head"><div><h3>Pipeline Conversion</h3><p>Overall recruiting funnel</p></div><button className="more-button"><Icon name="more"/></button></div>
    <div className="funnel">
      {steps.map(([name, count, rate, width]) => <div className="funnel-row" key={name}>
        <span>{name}</span><div className={`funnel-bar ${width}`}><strong>{count}</strong></div><b>{rate}</b>
      </div>)}
    </div>
    <div className="funnel-summary"><span className="kpi-icon green"><Icon name="chart" size={16}/></span><div><strong>4.2% overall conversion</strong><small>+0.8% from last month</small></div></div>
  </article>;
}

function Dashboard({ setScreen }: { setScreen: (s: Screen) => void }) {
  return <div className="page dashboard">
    <div className="page-heading"><div><span className="eyebrow">Tuesday, 18 February</span><h1>Good morning, Anna</h1><p>Here's what's happening with your hiring pipeline today.</p></div><button className="primary-button" onClick={() => setScreen("create-job")}><Icon name="plus"/>Create job</button></div>
    <section className="kpi-grid">
      <KpiCard icon="briefcase" label="Open Jobs" value="12" trend="8.2%" note="vs. last month" color="indigo"/>
      <KpiCard icon="users" label="Active Candidates" value="148" trend="12.4%" note="vs. last month" color="blue"/>
      <KpiCard icon="calendar" label="Interviews This Week" value="9" note="3 scheduled today" color="amber"/>
      <KpiCard icon="clock" label="Avg. Time to Hire" value="21 days" trend="3 days faster" note="than last quarter" color="green"/>
    </section>
    <section className="analytics-grid"><ApplicationsChart/><FunnelChart/></section>
    <section className="lists-grid">
      <article className="panel matches-panel">
        <div className="panel-head"><div><div className="title-with-icon"><span className="sparkle-icon"><Icon name="sparkles" size={16}/></span><h3>Top AI Matches Today</h3></div><p>Best new candidates based on job requirements</p></div><button className="link-button" onClick={() => setScreen("pipeline")}>View all <Icon name="arrow" size={15}/></button></div>
        <div className="match-list">{candidates.map((candidate, i) => <div className="match-row" key={candidate.name}>
          <Avatar initials={candidate.initials} color={candidate.color}/><div className="match-info"><strong>{candidate.name}</strong><span>{candidate.role}</span></div>
          <div className="match-job"><span>Matched for</span><strong>Senior Java Developer</strong></div>
          <span className={`score-pill ${i === 2 ? "good" : ""}`}><Icon name="sparkles" size={13}/>{candidate.score}% match</span>
          <button className="secondary-button" onClick={() => candidate.name === "Lena Schmidt" && setScreen("candidate")}>View</button>
        </div>)}</div>
      </article>
      <article className="panel interviews-panel">
        <div className="panel-head"><div><h3>Upcoming Interviews</h3><p>Your schedule this week</p></div><button className="icon-button"><Icon name="calendar"/></button></div>
        <div className="date-heading"><span>Today</span><b>3 interviews</b></div>
        {[["10:00", "Lena Schmidt", "Technical interview", "MR"], ["13:30", "Amira Haddad", "Culture add interview", "JW"], ["16:00", "Felix Wagner", "Hiring manager call", "AK"]].map(([time, name, type, interviewer], i) => <div className="interview-row" key={name}>
          <div className="time"><strong>{time}</strong><span>{i === 0 ? "60 min" : "45 min"}</span></div><i className={`timeline-dot dot-${i}`}></i>
          <Avatar initials={name.split(" ").map(x => x[0]).join("")} color={i === 0 ? "rose" : i === 1 ? "violet" : "blue"} small/>
          <div className="interview-info"><strong>{name}</strong><span>{type}</span></div><Avatar initials={interviewer} color="gray" small/>
        </div>)}
        <button className="full-link">Open calendar <Icon name="arrow" size={15}/></button>
      </article>
    </section>
  </div>;
}

function CandidateCard({ person, onOpen }: { person: typeof pipeline[number]["people"][number]; onOpen: () => void }) {
  const scoreTone = person.score >= 90 ? "excellent" : person.score >= 80 ? "good" : "low";
  return <article className="candidate-card" draggable onDoubleClick={onOpen}>
    <div className="candidate-card-head"><Avatar initials={person.initials} color={person.score >= 90 ? "rose" : "blue"}/><button className="more-button"><Icon name="more"/></button></div>
    <strong className="candidate-name">{person.name}</strong><span className="candidate-role">{person.role}</span>
    <div className="skills">{person.tags.map(tag => <span key={tag}>{tag}</span>)}</div>
    <div className="candidate-foot"><span className={`circle-score ${scoreTone}`}>{person.score}</span><span><Icon name="clock" size={14}/>{person.days === 0 ? "Today" : `${person.days}d in stage`}</span><Avatar initials="AK" color="gray" small/></div>
  </article>;
}

function Pipeline({ setScreen }: { setScreen: (s: Screen) => void }) {
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
        <div className="column-cards">{column.people.map(person => <CandidateCard key={person.name} person={person} onOpen={() => person.name === "Lena Schmidt" && setScreen("candidate")}/>)}
          {column.people.length === 0 && <div className="empty-column">No matching candidates</div>}
        </div>
      </section>)}
    </div>
  </div>;
}

const jobRows = [
  { title: "Senior Java Developer", department: "Engineering", location: "Berlin, Germany", mode: "Hybrid", applicants: 42, status: "Published", created: "14 Feb 2026" },
  { title: "Product Designer", department: "Product & Design", location: "Munich, Germany", mode: "Hybrid", applicants: 31, status: "Published", created: "11 Feb 2026" },
  { title: "Enterprise Account Executive", department: "Sales", location: "Hamburg, Germany", mode: "Remote", applicants: 18, status: "Published", created: "08 Feb 2026" },
  { title: "People Operations Manager", department: "People", location: "Berlin, Germany", mode: "On-site", applicants: 0, status: "Draft", created: "06 Feb 2026" },
  { title: "Senior Data Analyst", department: "Data", location: "Cologne, Germany", mode: "Remote", applicants: 27, status: "Published", created: "29 Jan 2026" },
  { title: "DevOps Engineer", department: "Engineering", location: "Frankfurt, Germany", mode: "Hybrid", applicants: 36, status: "Published", created: "24 Jan 2026" },
  { title: "Customer Success Manager", department: "Customer Success", location: "Munich, Germany", mode: "On-site", applicants: 54, status: "Closed", created: "12 Jan 2026" },
  { title: "Frontend Engineer", department: "Engineering", location: "Berlin, Germany", mode: "Remote", applicants: 63, status: "Closed", created: "05 Jan 2026" },
];

function Jobs({ setScreen }: { setScreen: (s: Screen) => void }) {
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("All statuses");
  const visibleJobs = jobRows.filter(job =>
    job.title.toLowerCase().includes(query.toLowerCase()) && (status === "All statuses" || job.status === status)
  );
  return <div className="page jobs-page">
    <div className="page-heading jobs-heading"><div><span className="eyebrow">Recruitment</span><h1>Jobs</h1><p>Manage job postings and track applicants across your hiring team.</p></div><button className="primary-button" onClick={() => setScreen("create-job")}><Icon name="plus"/>Create Job</button></div>
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
            <td><button className="job-link" onClick={() => job.title === "Senior Java Developer" ? setScreen("pipeline") : undefined}><span className="job-table-icon"><Icon name="briefcase" size={16}/></span><span><strong>{job.title}</strong><small>{job.status === "Draft" ? "Not visible to candidates" : "careers.talentflow.eu"}</small></span></button></td>
            <td>{job.department}</td><td>{job.location}</td>
            <td><span className={`work-badge ${job.mode.toLowerCase().replace("-", "")}`}><i></i>{job.mode}</span></td>
            <td><button className="applicant-count" onClick={() => setScreen("pipeline")}><span>{job.applicants}</span>{job.applicants === 1 ? "applicant" : "applicants"}</button></td>
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

function CreateJob({ setScreen }: { setScreen: (s: Screen) => void }) {
  const [skills, setSkills] = useState(["Java", "Spring Boot", "PostgreSQL"]);
  const [skillInput, setSkillInput] = useState("");
  const [saved, setSaved] = useState("");
  const addSkill = () => {
    const skill = skillInput.trim();
    if (skill && !skills.includes(skill)) setSkills([...skills, skill]);
    setSkillInput("");
  };
  const save = (type: string) => {
    setSaved(type);
    window.setTimeout(() => setScreen("jobs"), 700);
  };
  return <div className="page create-job-page">
    <div className="breadcrumbs"><button onClick={() => setScreen("jobs")}>Jobs</button><Icon name="chevron" size={13}/><strong>Create new job</strong></div>
    <div className="create-job-heading"><div><h1>Create a new job</h1><p>Define the role, requirements and compensation. You can save your progress at any time.</p></div><span><Icon name="shield" size={15}/>GDPR-ready application workflow</span></div>
    <form className="job-form" onSubmit={(e) => { e.preventDefault(); save("published"); }}>
      <section className="form-card panel">
        <div className="form-card-head"><span>1</span><div><h2>Job details</h2><p>Basic information candidates will see on the job posting.</p></div></div>
        <div className="form-card-body form-grid">
          <label className="full-field">Job title<span>*</span><input required defaultValue="Senior Java Developer" placeholder="e.g. Senior Java Developer"/></label>
          <label>Department<span>*</span><select required defaultValue="Engineering"><option>Engineering</option><option>Product & Design</option><option>Sales</option><option>People</option><option>Data</option><option>Customer Success</option></select></label>
          <label>Location<span>*</span><div className="input-with-icon"><Icon name="map" size={15}/><input required defaultValue="Berlin, Germany" placeholder="City, country"/></div></label>
          <fieldset className="full-field work-mode-field"><legend>Work mode<span>*</span></legend><div>
            {["Remote", "Hybrid", "On-site"].map((mode) => <label key={mode}><input type="radio" name="work-mode" value={mode} defaultChecked={mode === "Hybrid"}/><span><Icon name={mode === "Remote" ? "globe" : mode === "Hybrid" ? "users" : "briefcase"}/><strong>{mode}</strong><small>{mode === "Remote" ? "Work from anywhere" : mode === "Hybrid" ? "Office and remote" : "Office-based role"}</small></span></label>)}
          </div></fieldset>
        </div>
      </section>
      <section className="form-card panel">
        <div className="form-card-head"><span>2</span><div><h2>Compensation & skills</h2><p>Set a transparent salary range and core role requirements.</p></div></div>
        <div className="form-card-body form-grid">
          <div className="full-field salary-group"><label>Salary range in EUR</label><div><label><span>Minimum</span><div className="currency-input"><b>€</b><input type="number" defaultValue="75000"/></div></label><i>to</i><label><span>Maximum</span><div className="currency-input"><b>€</b><input type="number" defaultValue="95000"/></div></label><select aria-label="Salary period"><option>per year</option><option>per month</option></select></div><small>Salary transparency supports compliance with the EU Pay Transparency Directive.</small></div>
          <label className="full-field">Required skills<span>*</span><div className="tag-input">{skills.map(skill => <span key={skill}>{skill}<button type="button" onClick={() => setSkills(skills.filter(item => item !== skill))}>×</button></span>)}<input value={skillInput} onChange={(e) => setSkillInput(e.target.value)} onKeyDown={(e) => { if (e.key === "Enter" || e.key === ",") { e.preventDefault(); addSkill(); }}} onBlur={addSkill} placeholder="Type a skill and press Enter"/></div><small>Add the skills that are essential for success in this role.</small></label>
        </div>
      </section>
      <section className="form-card panel">
        <div className="form-card-head"><span>3</span><div><h2>Job description</h2><p>Describe the opportunity, responsibilities and your team.</p></div></div>
        <div className="form-card-body">
          <label>Job description<span>*</span></label>
          <div className="rich-editor">
            <div className="editor-toolbar"><button type="button"><b>B</b></button><button type="button"><i>I</i></button><button type="button"><u>U</u></button><span></span><button type="button">H1</button><button type="button">H2</button><span></span><button type="button">• List</button><button type="button">1. List</button><span></span><button type="button"><Icon name="globe" size={14}/></button></div>
            <div className="editor-content" contentEditable suppressContentEditableWarning><h3>About the role</h3><p>We are looking for a Senior Java Developer to join our growing Engineering team in Berlin. You will design and build reliable services that power our European platform.</p><h3>What you will do</h3><ul><li>Design scalable backend services using Java and Spring Boot</li><li>Collaborate with product, design and platform teams</li><li>Mentor engineers and improve development practices</li></ul><h3>What you bring</h3><p>At least 5 years of backend development experience and a thoughtful, collaborative approach to engineering.</p></div>
          </div>
          <div className="editor-foot"><span><Icon name="info" size={13}/>Use inclusive, skills-focused language.</span><b>512 characters</b></div>
        </div>
      </section>
      <div className="job-form-actions"><button type="button" className="cancel-button" onClick={() => setScreen("jobs")}>Cancel</button><span></span><button type="button" className="secondary-button draft-button" onClick={() => save("draft")}><Icon name="file"/>Save as Draft</button><button type="submit" className="primary-button"><Icon name="check"/>{saved === "published" ? "Publishing..." : "Publish"}</button></div>
    </form>
  </div>;
}

function Rating({ value }: { value: number }) {
  return <span className="rating" aria-label={`${value} out of 5 stars`}>
    {[1, 2, 3, 4, 5].map((star) => <span className={star <= value ? "filled" : ""} key={star}><Icon name="star" size={13}/></span>)}
  </span>;
}

function CvPreview() {
  return <article className="cv-panel panel">
    <div className="cv-toolbar">
      <div><span className="pdf-icon">PDF</span><div><strong>Lena_Schmidt_CV.pdf</strong><small>Updated 2 days ago · 284 KB</small></div></div>
      <div><button className="icon-button" aria-label="Download CV"><Icon name="download"/></button><span>1 / 2</span><button className="zoom-button">−</button><b>85%</b><button className="zoom-button">+</button></div>
    </div>
    <div className="pdf-canvas">
      <div className="cv-page">
        <div className="cv-document-head">
          <div><h2>LENA<br/>SCHMIDT</h2><span>SENIOR BACKEND ENGINEER</span></div>
          <div><p>lena.schmidt@email.de</p><p>Berlin, Germany</p><p>+49 151 234 5678</p></div>
        </div>
        <div className="cv-document-body">
          <aside>
            <section><h4>PROFILE</h4><p>Backend engineer with 8+ years of experience building scalable, resilient services for European technology companies.</p></section>
            <section><h4>SKILLS</h4><strong>Languages</strong><p>Java, Kotlin, SQL</p><strong>Frameworks</strong><p>Spring Boot, Hibernate</p><strong>Infrastructure</strong><p>Docker, AWS, Terraform</p></section>
            <section><h4>EDUCATION</h4><strong>M.Sc. Computer Science</strong><p>Technische Universität Berlin<br/>2014–2016</p></section>
            <section><h4>LANGUAGES</h4><p>German — Native<br/>English — Fluent</p></section>
          </aside>
          <main>
            <section><h4>EXPERIENCE</h4>
              <div className="cv-job"><b>2021 — PRESENT</b><h3>Senior Backend Engineer</h3><span>Delivery Hero · Berlin</span><p>Led the design of high-volume Java microservices processing over 2M daily transactions. Improved system reliability to 99.98% and mentored five engineers.</p></div>
              <div className="cv-job"><b>2018 — 2021</b><h3>Backend Engineer</h3><span>Zalando · Berlin</span><p>Developed Spring Boot services for the European commerce platform. Introduced automated testing practices and reduced deployment time by 40%.</p></div>
              <div className="cv-job"><b>2016 — 2018</b><h3>Software Engineer</h3><span>Otto Group · Hamburg</span><p>Built and maintained Java applications and PostgreSQL data pipelines for the logistics division.</p></div>
            </section>
            <section><h4>SELECTED PROJECT</h4><h3>Order Processing Platform</h3><p>Technical lead for a distributed event-driven system using Java, Spring Boot, PostgreSQL, Docker and Kafka.</p></section>
          </main>
        </div>
      </div>
    </div>
  </article>;
}

function AiAnalysis() {
  return <article className="analysis-card panel">
    <div className="analysis-heading"><span className="sparkle-icon"><Icon name="sparkles" size={17}/></span><div><h3>AI Screening Analysis</h3><p>Compared with job requirements</p></div><span className="analysis-version">v2.1</span></div>
    <div className="match-score-block">
      <div className="large-score"><svg viewBox="0 0 120 120"><circle cx="60" cy="60" r="52"/><circle className="score-progress" cx="60" cy="60" r="52"/></svg><div><strong>87%</strong><span>Strong match</span></div></div>
      <p>Based on experience, technical skills and role requirements.</p>
    </div>
    <div className="analysis-section"><div className="analysis-section-title"><strong>Matching skills</strong><span>4 of 5 required</span></div><div className="analysis-tags matching">{["Java", "Spring Boot", "PostgreSQL", "Docker"].map(skill => <span key={skill}><Icon name="check" size={12}/>{skill}</span>)}</div></div>
    <div className="analysis-section"><div className="analysis-section-title"><strong>Missing skills</strong><span>1 identified</span></div><div className="analysis-tags missing"><span>Kubernetes</span></div></div>
    <div className="ai-summary"><strong><Icon name="sparkles" size={14}/>AI summary</strong><p>Lena brings strong senior-level Java and Spring Boot experience from high-scale product companies. Her PostgreSQL and Docker background aligns closely with the core role requirements. Kubernetes is not listed, but her adjacent cloud infrastructure experience suggests a manageable learning curve.</p></div>
    <div className="ai-notice"><Icon name="info" size={17}/><p><strong>Human decision required</strong>AI suggestions support your decision. Final decisions are always made by a human. <button>Learn about the EU AI Act</button></p></div>
  </article>;
}

function CandidateTabs() {
  const [tab, setTab] = useState("Activity Timeline");
  return <section className="candidate-tabs panel">
    <div className="tabs" role="tablist">{["Activity Timeline", "Interview Feedback", "Notes"].map(name => <button role="tab" aria-selected={tab === name} className={tab === name ? "active" : ""} onClick={() => setTab(name)} key={name}>{name}{name === "Notes" && <b>2</b>}</button>)}</div>
    {tab === "Activity Timeline" && <div className="timeline">
      {[
        ["Interview scheduled", "Anna Keller", "Today, 09:42", "calendar", "Technical interview with Martin Richter for 20 Feb, 10:00"],
        ["Moved to Interview", "Jonas Weber", "Yesterday, 16:18", "arrow", "Stage changed from Screening to Interview"],
        ["CV reviewed", "Anna Keller", "17 Feb, 11:05", "eye", "AI screening analysis reviewed"],
        ["Application received", "Lena Schmidt", "16 Feb, 08:34", "file", "Applied via TalentFlow Careers"],
      ].map(([title, by, date, icon, detail]) => <div className="timeline-item" key={title}><span className="timeline-icon"><Icon name={icon as IconName} size={15}/></span><div><div><strong>{title}</strong><span>{date}</span></div><p>{detail}</p><small>by {by}</small></div></div>)}
    </div>}
    {tab === "Interview Feedback" && <div className="feedback-list">
      <article><Avatar initials="MR" color="blue"/><div><div><strong>Martin Richter</strong><span>Technical Interview · 18 Feb</span></div><Rating value={5}/><p>Excellent system design fundamentals and very strong Java knowledge. Communicates trade-offs clearly and pragmatically.</p></div><b>Strong hire</b></article>
      <article><Avatar initials="JW" color="violet"/><div><div><strong>Jonas Weber</strong><span>Recruiter Screen · 17 Feb</span></div><Rating value={4}/><p>Motivated by the product mission and looking for long-term technical ownership. Salary expectations are within range.</p></div><b>Proceed</b></article>
    </div>}
    {tab === "Notes" && <div className="notes-content"><textarea placeholder="Add a private note for your hiring team..."></textarea><div><span><Icon name="lock" size={13}/>Visible only to the hiring team</span><button className="primary-button">Add note</button></div></div>}
  </section>;
}

function CandidateDetail({ setScreen }: { setScreen: (s: Screen) => void }) {
  const [stage, setStage] = useState("Interview");
  return <div className="page candidate-page">
    <div className="breadcrumbs"><button onClick={() => setScreen("pipeline")}>Candidates</button><Icon name="chevron" size={13}/><span>Senior Java Developer</span><Icon name="chevron" size={13}/><strong>Lena Schmidt</strong></div>
    <header className="candidate-header">
      <div className="candidate-identity"><Avatar initials="LS" color="rose"/><div><div><h1>Lena Schmidt</h1><span className="gdpr-mini"><Icon name="shield" size={12}/>GDPR consent verified</span></div><p><Icon name="mail" size={13}/>lena.schmidt@email.de <i></i><Icon name="map" size={13}/>Berlin, Germany</p></div></div>
      <div className="candidate-actions"><label>Current stage<select value={stage} onChange={(e) => setStage(e.target.value)}><option>Applied</option><option>Screening</option><option>Interview</option><option>Offer</option><option>Hired</option></select></label><button className="secondary-button"><Icon name="calendar"/>Schedule Interview</button><button className="primary-button">Move to Next Stage<Icon name="arrow"/></button><button className="reject-button">Reject</button></div>
    </header>
    <div className="candidate-meta"><span>Applied for <strong>Senior Java Developer – Berlin</strong></span><span>Applied 16 February 2026</span><span className="gdpr-retention"><Icon name="shield" size={14}/>Consent given on 03/10/2026 · Data retained until 03/04/2027</span></div>
    <div className="candidate-detail-grid"><CvPreview/><AiAnalysis/></div>
    <CandidateTabs/>
  </div>;
}

export default function App() {
  const [screen, setScreen] = useState<Screen>("dashboard");
  const [collapsed, setCollapsed] = useState(false);
  const [dark, setDark] = useState(false);
  const [role, setRole] = useState("Recruiter");
  if (screen === "login") return <Login onLogin={() => setScreen("dashboard")}/>;
  return <div className={`app-shell ${dark ? "dark" : ""}`}>
    <Sidebar screen={screen} setScreen={setScreen} collapsed={collapsed} setCollapsed={setCollapsed} role={role} setRole={setRole}/>
    <div className="app-main">
      <Topbar dark={dark} setDark={setDark} setScreen={setScreen}/>
      {screen === "jobs" ? <Jobs setScreen={setScreen}/> :
        screen === "create-job" ? <CreateJob setScreen={setScreen}/> :
        screen === "pipeline" ? <Pipeline setScreen={setScreen}/> :
        screen === "candidate" ? <CandidateDetail setScreen={setScreen}/> :
        <Dashboard setScreen={setScreen}/>}
    </div>
  </div>;
}
