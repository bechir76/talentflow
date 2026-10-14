import { useDraggable } from "@dnd-kit/core";
import { CSS } from "@dnd-kit/utilities";
import { useTranslation } from "react-i18next";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { scoreTone } from "@/features/candidates/score";
import { initialsOf } from "@/lib/format";
import type { PipelineCard } from "../api/pipeline";

const DAY_MS = 24 * 60 * 60 * 1000;

function AiScoreBadge({ card }: { card: PipelineCard }) {
  const { t } = useTranslation();
  if (card.aiStatus === "DONE" && card.aiScore != null) {
    return <span className={`circle-score ${scoreTone(card.aiScore)}`} title={t("pipeline.aiScore")}>{card.aiScore}</span>;
  }
  return <span className="circle-score pending" title={t(card.aiStatus === "PENDING" ? "pipeline.aiPending" : "pipeline.aiFailed")}>{card.aiStatus === "PENDING" ? "…" : "–"}</span>;
}

type CandidateCardProps = { card: PipelineCard; draggable: boolean; onOpen: () => void; overlay?: boolean };

export default function CandidateCard({ card, draggable, onOpen, overlay = false }: CandidateCardProps) {
  const { t } = useTranslation();
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({ id: card.id, disabled: !draggable || overlay });
  const name = card.candidateName ?? t("pipeline.unknownCandidate");
  const days = Math.floor((Date.now() - new Date(card.stageChangedAt).getTime()) / DAY_MS);
  const score = card.aiScore ?? 0;

  return <article
    ref={setNodeRef}
    style={{ transform: CSS.Translate.toString(transform) }}
    className={`candidate-card ${isDragging ? "dragging" : ""} ${overlay ? "overlay" : ""} ${draggable ? "" : "locked"}`}
    onClick={onOpen}
    onKeyDown={(event) => { if (event.key === "Enter") onOpen(); }}
    aria-label={t("pipeline.cardLabel", { name })}
    {...attributes}
    {...listeners}
  >
    <div className="candidate-card-head"><Avatar initials={initialsOf(name)} color={score >= 80 ? "rose" : "blue"}/></div>
    <strong className="candidate-name">{name}</strong><span className="candidate-role">{card.candidateLocation ?? " "}</span>
    <div className="skills">{card.topSkills.map((tag) => <span key={tag}>{tag}</span>)}</div>
    <div className="candidate-foot"><AiScoreBadge card={card}/><span><Icon name="clock" size={14}/>{days === 0 ? t("pipeline.today") : t("pipeline.daysInStage", { count: days })}</span></div>
  </article>;
}
