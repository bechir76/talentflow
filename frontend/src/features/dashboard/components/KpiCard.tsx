import Icon, { type IconName } from "@/components/common/Icon";

type KpiCardProps = { icon: IconName; label: string; value: string; trend?: string; note: string; color: string };

export default function KpiCard({ icon, label, value, trend, note, color }: KpiCardProps) {
  return <article className="kpi-card">
    <div className="kpi-top"><span className={`kpi-icon ${color}`}><Icon name={icon}/></span><button className="more-button"><Icon name="more"/></button></div>
    <span className="kpi-label">{label}</span><div className="kpi-value">{value}</div>
    <div className="kpi-foot">{trend && <span className="trend">↗ {trend}</span>}<span>{note}</span></div>
  </article>;
}
