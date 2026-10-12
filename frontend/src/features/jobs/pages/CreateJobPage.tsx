import { useState } from "react";
import { useNavigate } from "react-router";
import Icon from "@/components/common/Icon";

export default function CreateJobPage() {
  const navigate = useNavigate();
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
    window.setTimeout(() => navigate("/jobs"), 700);
  };
  return <div className="page create-job-page">
    <div className="breadcrumbs"><button onClick={() => navigate("/jobs")}>Jobs</button><Icon name="chevron" size={13}/><strong>Create new job</strong></div>
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
      <div className="job-form-actions"><button type="button" className="cancel-button" onClick={() => navigate("/jobs")}>Cancel</button><span></span><button type="button" className="secondary-button draft-button" onClick={() => save("draft")}><Icon name="file"/>Save as Draft</button><button type="submit" className="primary-button"><Icon name="check"/>{saved === "published" ? "Publishing..." : "Publish"}</button></div>
    </form>
  </div>;
}
