import Icon from "@/components/common/Icon";
import { funnelSteps } from "../mock";

export default function FunnelChart() {
  return <article className="panel funnel-panel">
    <div className="panel-head"><div><h3>Pipeline Conversion</h3><p>Overall recruiting funnel</p></div><button className="more-button"><Icon name="more"/></button></div>
    <div className="funnel">
      {funnelSteps.map(([name, count, rate, width]) => <div className="funnel-row" key={name}>
        <span>{name}</span><div className={`funnel-bar ${width}`}><strong>{count}</strong></div><b>{rate}</b>
      </div>)}
    </div>
    <div className="funnel-summary"><span className="kpi-icon green"><Icon name="chart" size={16}/></span><div><strong>4.2% overall conversion</strong><small>+0.8% from last month</small></div></div>
  </article>;
}
