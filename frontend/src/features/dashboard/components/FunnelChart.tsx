import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import type { Dashboard } from "../api/dashboard";

const MIN_BAR_PERCENT = 12;

/** Pipeline funnel: how many applications reached each stage, with the conversion from the previous one. */
export default function FunnelChart({ funnel }: { funnel: Dashboard["funnel"] }) {
  const { t } = useTranslation();
  const applied = funnel[0]?.applications ?? 0;
  const hired = funnel[funnel.length - 1]?.applications ?? 0;
  const overall = applied > 0 ? (100 * hired) / applied : 0;
  return <article className="panel funnel-panel">
    <div className="panel-head"><div><h3>{t("dashboard.funnelTitle")}</h3><p>{t("dashboard.funnelSubtitle")}</p></div></div>
    <div className="funnel">
      {funnel.map((step, index) => {
        const previous = index === 0 ? step.applications : funnel[index - 1]!.applications;
        const rate = previous > 0 ? Math.round((100 * step.applications) / previous) : 0;
        const width = applied > 0 ? Math.max(MIN_BAR_PERCENT, (100 * step.applications) / applied) : MIN_BAR_PERCENT;
        return <div className="funnel-row" key={step.stage}>
          <span>{t(`pipeline.stages.${step.stage}`)}</span>
          <div className={`funnel-bar funnel-${index + 1}`} style={{ width: `${width}%` }}><strong>{step.applications}</strong></div>
          <b>{index === 0 ? "100%" : `${rate}%`}</b>
        </div>;
      })}
    </div>
    <div className="funnel-summary"><span className="kpi-icon green"><Icon name="chart" size={16}/></span><div><strong>{t("dashboard.overallConversion", { rate: overall.toFixed(1) })}</strong><small>{t("dashboard.overallNote")}</small></div></div>
  </article>;
}
