import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import type { PipelineCandidate } from "../mock";

export default function CandidateCard({ person, onOpen }: { person: PipelineCandidate; onOpen: () => void }) {
  const scoreTone = person.score >= 90 ? "excellent" : person.score >= 80 ? "good" : "low";
  return <article className="candidate-card" draggable onDoubleClick={onOpen}>
    <div className="candidate-card-head"><Avatar initials={person.initials} color={person.score >= 90 ? "rose" : "blue"}/><button className="more-button"><Icon name="more"/></button></div>
    <strong className="candidate-name">{person.name}</strong><span className="candidate-role">{person.role}</span>
    <div className="skills">{person.tags.map(tag => <span key={tag}>{tag}</span>)}</div>
    <div className="candidate-foot"><span className={`circle-score ${scoreTone}`}>{person.score}</span><span><Icon name="clock" size={14}/>{person.days === 0 ? "Today" : `${person.days}d in stage`}</span><Avatar initials="AK" color="gray" small/></div>
  </article>;
}
