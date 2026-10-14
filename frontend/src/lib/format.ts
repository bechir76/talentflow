import { currentLanguage } from "@/lib/i18n";

function locale(): string {
  return currentLanguage() === "de" ? "de-DE" : "en-GB";
}

export function formatDate(iso: string): string {
  return new Intl.DateTimeFormat(locale(), { day: "2-digit", month: "short", year: "numeric" }).format(new Date(iso));
}

export function formatMoney(amount: number, currency = "EUR"): string {
  return new Intl.NumberFormat(locale(), { style: "currency", currency, maximumFractionDigits: 0 }).format(amount);
}

/** "€75,000 – €95,000", or undefined when no salary is given. */
export function formatSalaryRange(min: number | null, max: number | null, currency = "EUR"): string | undefined {
  if (min != null && max != null) return `${formatMoney(min, currency)} – ${formatMoney(max, currency)}`;
  if (min != null) return `≥ ${formatMoney(min, currency)}`;
  if (max != null) return `≤ ${formatMoney(max, currency)}`;
  return undefined;
}

const DAY_MS = 24 * 60 * 60 * 1000;

/** Whole days between an ISO timestamp and now (0 on the same day). */
export function daysSince(iso: string, now: number = Date.now()): number {
  return Math.max(0, Math.floor((now - new Date(iso).getTime()) / DAY_MS));
}

/** "Lena Schmidt" → "LS". */
export function initialsOf(name: string): string {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]!.toUpperCase()).join("") || "?";
}

/** Date and time in the app language, e.g. "13 Oct 2026, 10:00". */
export function formatDateTime(iso: string): string {
  return new Intl.DateTimeFormat(locale(), { dateStyle: "medium", timeStyle: "short" }).format(new Date(iso));
}

export function formatTime(iso: string): string {
  return new Intl.DateTimeFormat(locale(), { hour: "2-digit", minute: "2-digit" }).format(new Date(iso));
}

export function formatWeekday(iso: string): string {
  return new Intl.DateTimeFormat(locale(), { weekday: "short", day: "numeric" }).format(new Date(iso));
}
