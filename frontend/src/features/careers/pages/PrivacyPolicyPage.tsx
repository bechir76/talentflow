import { useTranslation } from "react-i18next";
import { Link } from "react-router";
import Icon from "@/components/common/Icon";
import { usePrivacyPolicy } from "../api/privacyPolicy";

const SECTIONS = ["data", "purposes", "ai", "retention", "recipients", "rights"] as const;

/** Public privacy notice (GDPR Art. 13): what is stored, why, for how long and which rights candidates have. */
export default function PrivacyPolicyPage() {
  const { t } = useTranslation();
  const facts = usePrivacyPolicy();
  const months = facts.data?.rejectedRetentionMonths;

  const retentionRule = facts.data == null ? t("privacyPolicy.retention.unknown")
    : facts.data.autoDeleteEnabled ? t("privacyPolicy.retention.automatic", { count: months })
      : t("privacyPolicy.retention.manual", { count: months });

  return <div className="career-detail privacy-policy">
    <div className="breadcrumbs"><Link to="/careers">{t("careers.title")}</Link><Icon name="chevron" size={13}/><strong>{t("privacyPolicy.title")}</strong></div>
    <header className="career-detail-head">
      <div>
        <span className="eyebrow">{t("privacyPolicy.eyebrow")}</span>
        <h1>{t("privacyPolicy.title")}</h1>
        <p><Icon name="shield" size={14}/>{facts.data ? t("privacyPolicy.version", { version: facts.data.version }) : t("privacyPolicy.intro")}</p>
      </div>
    </header>
    <article className="panel career-description">
      <p className="job-description">{t("privacyPolicy.summary")}</p>
      {SECTIONS.map((section) => <section key={section}>
        <h2>{t(`privacyPolicy.${section}.title`)}</h2>
        <ul>{(t(`privacyPolicy.${section}.items`, { returnObjects: true }) as string[]).map((item) => <li key={item}>{item}</li>)}</ul>
        {section === "retention" && <p className="job-description">{retentionRule}</p>}
      </section>)}
      <section>
        <h2>{t("privacyPolicy.contact.title")}</h2>
        <p className="job-description">{t("privacyPolicy.contact.text")}</p>
      </section>
    </article>
  </div>;
}
