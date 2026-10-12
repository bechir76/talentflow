import Icon from "@/components/common/Icon";

export default function AiAnalysis() {
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
