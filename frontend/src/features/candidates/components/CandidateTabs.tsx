import { useState } from "react";
import Avatar from "@/components/common/Avatar";
import Icon, { type IconName } from "@/components/common/Icon";
import Rating from "./Rating";

export default function CandidateTabs() {
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
