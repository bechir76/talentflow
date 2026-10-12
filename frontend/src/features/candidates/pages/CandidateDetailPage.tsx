import { useState } from "react";
import { useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import AiAnalysis from "../components/AiAnalysis";
import CandidateTabs from "../components/CandidateTabs";
import CvPreview from "../components/CvPreview";

export default function CandidateDetailPage() {
  const navigate = useNavigate();
  const [stage, setStage] = useState("Interview");
  return <div className="page candidate-page">
    <div className="breadcrumbs"><button onClick={() => navigate("/pipeline")}>Candidates</button><Icon name="chevron" size={13}/><span>Senior Java Developer</span><Icon name="chevron" size={13}/><strong>Lena Schmidt</strong></div>
    <header className="candidate-header">
      <div className="candidate-identity"><Avatar initials="LS" color="rose"/><div><div><h1>Lena Schmidt</h1><span className="gdpr-mini"><Icon name="shield" size={12}/>GDPR consent verified</span></div><p><Icon name="mail" size={13}/>lena.schmidt@email.de <i></i><Icon name="map" size={13}/>Berlin, Germany</p></div></div>
      <div className="candidate-actions"><label>Current stage<select value={stage} onChange={(e) => setStage(e.target.value)}><option>Applied</option><option>Screening</option><option>Interview</option><option>Offer</option><option>Hired</option></select></label><button className="secondary-button"><Icon name="calendar"/>Schedule Interview</button><button className="primary-button">Move to Next Stage<Icon name="arrow"/></button><button className="reject-button">Reject</button></div>
    </header>
    <div className="candidate-meta"><span>Applied for <strong>Senior Java Developer – Berlin</strong></span><span>Applied 16 February 2026</span><span className="gdpr-retention"><Icon name="shield" size={14}/>Consent given on 03/10/2026 · Data retained until 03/04/2027</span></div>
    <div className="candidate-detail-grid"><CvPreview/><AiAnalysis/></div>
    <CandidateTabs/>
  </div>;
}
