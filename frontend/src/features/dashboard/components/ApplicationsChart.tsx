import Icon from "@/components/common/Icon";
import { applicationsPerJob } from "../mock";

export default function ApplicationsChart() {
  return <article className="panel chart-panel">
    <div className="panel-head"><div><h3>Applications per Job</h3><p>New applications in the last 30 days</p></div><button className="select-button">Last 30 days <Icon name="chevron" size={13}/></button></div>
    <div className="bar-chart">
      <div className="y-axis"><span>120</span><span>90</span><span>60</span><span>30</span><span>0</span></div>
      <div className="bars">
        {applicationsPerJob.map(([name, height, count]) => <div className="bar-column" key={name}><div className="bar-value">{count}</div><div className="bar-track"><i style={{ height: `${height}%` }}></i></div><span>{name}</span></div>)}
      </div>
    </div>
  </article>;
}
