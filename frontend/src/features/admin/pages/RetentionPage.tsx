import { useState } from "react";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import { formatDate } from "@/lib/format";
import { useRetentionPolicy, useRunRetention, useUpdateRetentionPolicy, type RetentionPolicy } from "../api/admin";

const MIN_MONTHS = 1;
const MAX_MONTHS = 120;

export default function RetentionPage() {
  const { t } = useTranslation();
  const policy = useRetentionPolicy();
  const update = useUpdateRetentionPolicy();
  const run = useRunRetention();
  const [months, setMonths] = useState(6);
  const [enabled, setEnabled] = useState(false);

  const [loaded, setLoaded] = useState<RetentionPolicy>();

  // Copy the saved policy into the form whenever a new version arrives from the server.
  if (policy.data && policy.data !== loaded) {
    setLoaded(policy.data);
    setMonths(policy.data.rejectedRetentionMonths);
    setEnabled(policy.data.autoDeleteEnabled);
  }

  const valid = Number.isInteger(months) && months >= MIN_MONTHS && months <= MAX_MONTHS;

  return <div className="page create-job-page">
    <div className="page-heading"><div><span className="eyebrow">{t("admin.eyebrow")}</span><h1>{t("admin.retention.title")}</h1><p>{t("admin.retention.subtitle")}</p></div></div>
    <form className="job-form" onSubmit={(e) => { e.preventDefault(); if (valid) update.mutate({ rejectedRetentionMonths: months, autoDeleteEnabled: enabled }); }}>
      <section className="form-card panel">
        <div className="form-card-head"><span><Icon name="shield" size={14}/></span><div><h2>{t("admin.retention.policyTitle")}</h2><p>{t("admin.retention.policyText")}</p></div></div>
        <div className="form-card-body form-grid">
          <label>{t("admin.retention.months")}<input type="number" min={MIN_MONTHS} max={MAX_MONTHS} value={months} className={valid ? "" : "invalid"} onChange={(e) => setMonths(Number(e.target.value))}/>
            {!valid && <small className="field-error">{t("admin.retention.monthsInvalid")}</small>}</label>
          <label className="toggle-field">{t("admin.retention.auto")}
            <span className="toggle"><input type="checkbox" checked={enabled} onChange={(e) => setEnabled(e.target.checked)}/><i></i><b>{t(enabled ? "admin.retention.on" : "admin.retention.off")}</b></span>
          </label>
          <small className="full-field">{t("admin.retention.hint")}</small>
          {policy.data && <small className="full-field">{t("admin.retention.updated", { date: formatDate(policy.data.updatedAt) })}</small>}
        </div>
      </section>
      {update.isError && <div className="form-error-banner" role="alert"><Icon name="info" size={16}/>{update.error.message}</div>}
      <div className="job-form-actions">
        <span>{update.isSuccess && <span className="saved-hint"><Icon name="check" size={14}/>{t("admin.retention.saved")}</span>}</span>
        <button type="button" className="secondary-button draft-button" disabled={run.isPending} onClick={() => run.mutate()}><Icon name="clock"/>{t("admin.retention.runNow")}</button>
        <button type="submit" className="primary-button" disabled={!valid || update.isPending}><Icon name="check"/>{t("admin.retention.save")}</button>
      </div>
      {run.data && <p className="saved-hint" role="status"><Icon name="info" size={14}/>{t("admin.retention.runResult", { count: run.data.anonymizedCandidates })}</p>}
    </form>
  </div>;
}
