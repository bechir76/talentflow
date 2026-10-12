import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";
import Icon from "@/components/common/Icon";
import { currentLanguage } from "@/lib/i18n";
import { useCalendar, type CalendarEntry } from "../api/interviews";

const DAYS_PER_WEEK = 7;

function startOfWeek(date: Date): Date {
  const monday = new Date(date);
  monday.setHours(0, 0, 0, 0);
  monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7));
  return monday;
}

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

/** Week view of interviews. Hiring managers see their own, recruiters and admins everyone's. */
export default function CalendarPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [weekStart, setWeekStart] = useState(() => startOfWeek(new Date()));
  const weekEnd = addDays(weekStart, DAYS_PER_WEEK);
  const calendar = useCalendar(weekStart, weekEnd);
  const locale = currentLanguage() === "de" ? "de-DE" : "en-GB";
  const days = Array.from({ length: DAYS_PER_WEEK }, (_, i) => addDays(weekStart, i));
  const today = new Date().toDateString();

  const byDay = useMemo(() => {
    const groups = new Map<string, CalendarEntry[]>();
    for (const entry of calendar.data ?? []) {
      const key = new Date(entry.scheduledAt).toDateString();
      groups.set(key, [...(groups.get(key) ?? []), entry]);
    }
    return groups;
  }, [calendar.data]);

  const range = `${weekStart.toLocaleDateString(locale, { day: "numeric", month: "short" })} – ${addDays(weekEnd, -1).toLocaleDateString(locale, { day: "numeric", month: "short", year: "numeric" })}`;

  return <div className="page calendar-page">
    <div className="page-heading"><div><span className="eyebrow">{t("interviews.eyebrow")}</span><h1>{t("interviews.title")}</h1><p>{t("interviews.subtitle")}</p></div>
      <div className="calendar-nav">
        <button className="secondary-button" onClick={() => setWeekStart(addDays(weekStart, -DAYS_PER_WEEK))} aria-label={t("interviews.previousWeek")}><Icon name="chevron" size={14}/></button>
        <strong>{range}</strong>
        <button className="secondary-button" onClick={() => setWeekStart(addDays(weekStart, DAYS_PER_WEEK))} aria-label={t("interviews.nextWeek")}><Icon name="chevron" size={14}/></button>
        <button className="secondary-button" onClick={() => setWeekStart(startOfWeek(new Date()))}>{t("interviews.today")}</button>
      </div>
    </div>
    {calendar.isError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{calendar.error.message}</div>}
    <div className="week-grid">
      {days.map((day) => {
        const entries = byDay.get(day.toDateString()) ?? [];
        return <section key={day.toISOString()} className={`week-day panel ${day.toDateString() === today ? "today" : ""}`}>
          <header><span>{day.toLocaleDateString(locale, { weekday: "short" })}</span><strong>{day.getDate()}</strong></header>
          {entries.map((entry) => <button key={entry.id} className="calendar-event" onClick={() => navigate(`/applications/${entry.applicationId}`)}>
            <b>{new Date(entry.scheduledAt).toLocaleTimeString(locale, { hour: "2-digit", minute: "2-digit" })} · {t("interviews.minutes", { count: entry.durationMinutes })}</b>
            <strong>{entry.candidateName ?? t("pipeline.unknownCandidate")}</strong>
            <span>{entry.jobTitle}</span>
            <small>{entry.interviewers.map((i) => i.name).join(", ")}</small>
          </button>)}
          {entries.length === 0 && <span className="week-empty">{t("interviews.free")}</span>}
        </section>;
      })}
    </div>
  </div>;
}
