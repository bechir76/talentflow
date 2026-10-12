import { useState } from "react";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import Pagination from "@/components/common/Pagination";
import { AUDIT_ACTIONS, useAuditLog, type AuditAction } from "../api/admin";

export default function AuditLogPage() {
  const { t } = useTranslation();
  const [action, setAction] = useState<AuditAction>();
  const [page, setPage] = useState(0);
  const log = useAuditLog(action, page);
  const formatTime = (iso: string) => new Date(iso).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "medium" });

  return <div className="page jobs-page">
    <div className="page-heading"><div><span className="eyebrow">{t("admin.eyebrow")}</span><h1>{t("admin.audit.title")}</h1><p>{t("admin.audit.subtitle")}</p></div></div>
    <section className="jobs-table-panel panel">
      <div className="table-toolbar">
        <select value={action ?? ""} onChange={(e) => { setAction((e.target.value || undefined) as AuditAction | undefined); setPage(0); }} aria-label={t("admin.audit.action")}>
          <option value="">{t("admin.audit.allActions")}</option>
          {AUDIT_ACTIONS.map((a) => <option key={a} value={a}>{t(`admin.audit.actions.${a}`)}</option>)}
        </select>
        {log.data && <span>{t("admin.audit.count", { count: log.data.totalElements })}</span>}
      </div>
      <div className="jobs-table-wrap">
        <table className="jobs-table">
          <thead><tr><th>{t("admin.audit.time")}</th><th>{t("admin.audit.actor")}</th><th>{t("admin.audit.action")}</th><th>{t("admin.audit.entity")}</th><th>{t("admin.audit.details")}</th></tr></thead>
          <tbody>{log.data?.content.map((entry) => <tr key={entry.id}>
            <td>{formatTime(entry.timestamp)}</td>
            <td><strong className="audit-actor">{entry.actorName ?? entry.actorId}</strong></td>
            <td><span className={`audit-action ${entry.action.toLowerCase()}`}>{t(`admin.audit.actions.${entry.action}`)}</span></td>
            <td>{entry.entityType}{entry.entityId && <small className="audit-id">{entry.entityId.slice(0, 8)}</small>}</td>
            <td className="audit-details">{entry.details ?? "–"}</td>
          </tr>)}</tbody>
        </table>
        {log.isPending && <div className="table-empty"><strong>{t("common.loading")}</strong></div>}
        {log.data?.totalElements === 0 && <div className="table-empty"><Icon name="shield" size={24}/><strong>{t("admin.audit.empty")}</strong></div>}
      </div>
      {log.data && log.data.totalElements > 0 && <Pagination {...log.data} onPageChange={setPage}/>}
    </section>
  </div>;
}
