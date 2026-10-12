import { useTranslation } from "react-i18next";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { Dashboard } from "../api/dashboard";

const MAX_LABEL = 14;

function shorten(title: string): string {
  return title.length > MAX_LABEL ? `${title.slice(0, MAX_LABEL - 1)}…` : title;
}

/** Applications per job in the last 30 days, styled like the design's bar chart. */
export default function ApplicationsChart({ data }: { data: Dashboard["applicationsPerJob"] }) {
  const { t } = useTranslation();
  const rows = data.map((row) => ({ name: shorten(row.jobTitle), fullName: row.jobTitle, applications: row.applications }));
  return <article className="panel chart-panel">
    <div className="panel-head"><div><h3>{t("dashboard.chartTitle")}</h3><p>{t("dashboard.chartSubtitle")}</p></div></div>
    {rows.length === 0
      ? <div className="table-empty chart-empty"><strong>{t("dashboard.chartEmpty")}</strong></div>
      : <div className="bar-chart-recharts">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={rows} margin={{ top: 18, right: 4, bottom: 0, left: -18 }}>
            <defs>
              <linearGradient id="barFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#766ff0"/>
                <stop offset="100%" stopColor="#4f46e5"/>
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} className="chart-grid"/>
            <XAxis dataKey="name" tickLine={false} axisLine={false} tick={{ className: "chart-tick" }} interval={0}/>
            <YAxis allowDecimals={false} tickLine={false} axisLine={false} tick={{ className: "chart-tick" }}/>
            <Tooltip cursor={{ className: "chart-cursor" }} labelFormatter={(_label, payload) => payload?.[0]?.payload?.fullName ?? ""} formatter={(value) => [value, t("dashboard.applications")]} contentStyle={{ border: 0, borderRadius: 6, background: "#252a38", color: "white", fontSize: 10 }} itemStyle={{ color: "white" }} labelStyle={{ color: "#c9cbd6" }}/>
            <Bar dataKey="applications" fill="url(#barFill)" radius={[5, 5, 1, 1]} maxBarSize={25}/>
          </BarChart>
        </ResponsiveContainer>
      </div>}
  </article>;
}
