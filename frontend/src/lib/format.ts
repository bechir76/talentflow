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
