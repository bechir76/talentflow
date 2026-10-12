import { useState } from "react";
import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";
import Modal from "@/components/common/Modal";
import { useAuth } from "@/lib/auth/AuthContext";
import { formatDate } from "@/lib/format";
import { useConsents, useDataExport, useDeleteMyData } from "../api/privacy";

/** Candidate self-service for GDPR rights: access (export), erasure and consent history. */
export default function PrivacyPage() {
  const { t } = useTranslation();
  const { logout } = useAuth();
  const consents = useConsents();
  const dataExport = useDataExport();
  const deleteData = useDeleteMyData();
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [understood, setUnderstood] = useState(false);

  const confirmDelete = () => deleteData.mutate(undefined, { onSuccess: () => { setConfirmOpen(false); logout(); } });

  return <div className="page portal-page">
    <div className="page-heading"><div><span className="eyebrow">{t("portal.eyebrow")}</span><h1>{t("privacy.title")}</h1><p>{t("privacy.subtitle")}</p></div></div>
    <div className="privacy-grid">
      <section className="panel privacy-card">
        <span className="kpi-icon indigo"><Icon name="download"/></span>
        <h2>{t("privacy.exportTitle")}</h2>
        <p>{t("privacy.exportText")}</p>
        <button className="primary-button" disabled={dataExport.isPending} onClick={() => dataExport.mutate()}><Icon name="download" size={15}/>{t("privacy.exportButton")}</button>
        {dataExport.isError && <small className="field-error" role="alert">{dataExport.error.message}</small>}
      </section>
      <section className="panel privacy-card danger">
        <span className="kpi-icon red"><Icon name="lock"/></span>
        <h2>{t("privacy.deleteTitle")}</h2>
        <p>{t("privacy.deleteText")}</p>
        <button className="reject-button" onClick={() => { setUnderstood(false); setConfirmOpen(true); }}>{t("privacy.deleteButton")}</button>
      </section>
    </div>
    <section className="panel consent-history">
      <div className="panel-head"><div><h3>{t("privacy.consentTitle")}</h3><p>{t("privacy.consentText")}</p></div></div>
      <div className="jobs-table-wrap">
        <table className="jobs-table compact">
          <thead><tr><th>{t("privacy.consentType")}</th><th>{t("privacy.policyVersion")}</th><th>{t("privacy.givenAt")}</th><th>{t("privacy.status")}</th></tr></thead>
          <tbody>{consents.data?.map((consent) => <tr key={consent.givenAt + consent.type}>
            <td>{t(`privacy.types.${consent.type}`, { defaultValue: consent.type })}</td><td>{consent.policyVersion}</td><td>{formatDate(consent.givenAt)}</td>
            <td>{consent.withdrawnAt ? <span className="job-status closed"><i></i>{t("privacy.withdrawn", { date: formatDate(consent.withdrawnAt) })}</span> : <span className="job-status published"><i></i>{t("privacy.active")}</span>}</td>
          </tr>)}</tbody>
        </table>
        {consents.data?.length === 0 && <div className="table-empty"><strong>{t("privacy.noConsents")}</strong></div>}
      </div>
    </section>
    <Modal open={confirmOpen} title={t("privacy.confirmTitle")} onClose={() => setConfirmOpen(false)} footer={<>
      <button className="cancel-button" onClick={() => setConfirmOpen(false)}>{t("common.cancel")}</button>
      <button className="reject-button" disabled={!understood || deleteData.isPending} onClick={confirmDelete}>{t("privacy.confirmButton")}</button>
    </>}>
      <p className="modal-text">{t("privacy.confirmText")}</p>
      <label className="consent-box"><input type="checkbox" checked={understood} onChange={(e) => setUnderstood(e.target.checked)}/><span><strong>{t("privacy.confirmCheck")}</strong></span></label>
      {deleteData.isError && <p className="field-error" role="alert">{deleteData.error.message}</p>}
    </Modal>
  </div>;
}
