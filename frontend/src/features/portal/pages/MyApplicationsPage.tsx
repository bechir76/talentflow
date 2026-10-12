import { useTranslation } from "react-i18next";
import Icon from "@/components/common/Icon";

/** Candidate home. The application list arrives with the applications feature. */
export default function MyApplicationsPage() {
  const { t } = useTranslation();
  return <div className="page">
    <div className="page-heading"><div><span className="eyebrow">{t("portal.eyebrow")}</span><h1>{t("portal.title")}</h1><p>{t("portal.subtitle")}</p></div></div>
    <section className="panel">
      <div className="table-empty"><Icon name="file" size={24}/><strong>{t("portal.emptyTitle")}</strong><span>{t("portal.emptyText")}</span></div>
    </section>
  </div>;
}
