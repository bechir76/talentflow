import Icon, { type IconName } from "@/components/common/Icon";

type KpiCardProps = { icon: IconName; label: string; value: string; note: string; color: string };

export default function KpiCard({ icon, label, value, note, color }: KpiCardProps) {
  return <article className="kpi-card">
    <div className="kpi-top"><span className={`kpi-icon ${color}`}><Icon name={icon}/></span></div>
    <span className="kpi-label">{label}</span><div className="kpi-value">{value}</div>
    <div className="kpi-foot"><span>{note}</span></div>
  </article>;
}
