import { useState, type ReactNode } from "react";
import { useTranslation } from "react-i18next";
import Icon, { type IconName } from "@/components/common/Icon";

export type TimelineEntry = { id: string; icon: IconName; title: string; detail?: string; by?: string; at: string };

type Tab = "timeline" | "feedback";

type CandidateTabsProps = { timeline: TimelineEntry[]; formatAt: (iso: string) => string; interviews: ReactNode };

export default function CandidateTabs({ timeline, formatAt, interviews }: CandidateTabsProps) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<Tab>("timeline");
  const tabs: { id: Tab; label: string; badge?: ReactNode }[] = [
    { id: "timeline", label: t("candidate.tabs.timeline") },
    { id: "feedback", label: t("candidate.tabs.feedback") },
  ];
  return <section className="candidate-tabs panel">
    <div className="tabs" role="tablist">{tabs.map((item) => <button role="tab" aria-selected={tab === item.id} className={tab === item.id ? "active" : ""} onClick={() => setTab(item.id)} key={item.id}>{item.label}</button>)}</div>
    {tab === "timeline" && <div className="timeline">
      {timeline.map((entry) => <div className="timeline-item" key={entry.id}><span className="timeline-icon"><Icon name={entry.icon} size={15}/></span><div><div><strong>{entry.title}</strong><span>{formatAt(entry.at)}</span></div>{entry.detail && <p>{entry.detail}</p>}{entry.by && <small>{t("candidate.timeline.by", { name: entry.by })}</small>}</div></div>)}
    </div>}
    {tab === "feedback" && interviews}
  </section>;
}
