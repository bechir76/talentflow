import { useTranslation } from "react-i18next";
import type { JobStatus, WorkMode } from "../api/jobs";

export function WorkModeBadge({ mode }: { mode: WorkMode }) {
  const { t } = useTranslation();
  return <span className={`work-badge ${mode.toLowerCase()}`}><i></i>{t(`jobs.workMode.${mode}`)}</span>;
}

export function JobStatusBadge({ status }: { status: JobStatus }) {
  const { t } = useTranslation();
  return <span className={`job-status ${status.toLowerCase()}`}><i></i>{t(`jobs.status.${status}`)}</span>;
}
