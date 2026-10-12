import { useTranslation } from "react-i18next";
import { Navigate, useLocation } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import Logo from "@/components/common/Logo";
import { useAuth } from "@/lib/auth/AuthContext";
import { homePathFor } from "@/lib/auth/roles";
import { changeLanguage, currentLanguage } from "@/lib/i18n";

export default function LoginPage() {
  const { t } = useTranslation();
  const { state, login, register } = useAuth();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;

  if (state.status === "authenticated") {
    return <Navigate to={from ?? homePathFor(state.user.primaryRole)} replace/>;
  }

  return <main className="login-page">
    <section className="login-brand">
      <Logo />
      <div className="login-copy">
        <span className="eyebrow light"><Icon name="sparkles" size={15}/> {t("auth.eyebrow")}</span>
        <h1>{t("auth.headlineLine1")}<br/>{t("auth.headlineLine2")}</h1>
        <p>{t("auth.tagline")}</p>
      </div>
      <div className="login-art" aria-hidden="true">
        <div className="art-orbit orbit-one"></div><div className="art-orbit orbit-two"></div>
        <div className="art-card art-card-main">
          <div className="art-title"><span className="mini-logo">T</span><span>{t("auth.artMatch")}</span><b>94%</b></div>
          <div className="art-profile"><Avatar initials="LS" color="rose"/><div><strong>Lena Schmidt</strong><small>Senior Backend Engineer</small></div></div>
          <div className="art-bars"><i></i><i></i><i></i></div>
          <div className="art-tags"><span>Java</span><span>Spring Boot</span><span>Docker</span></div>
        </div>
        <div className="art-card art-card-small"><span className="success-dot"><Icon name="check" size={13}/></span><div><strong>{t("auth.artGdprTitle")}</strong><small>{t("auth.artGdprText")}</small></div></div>
        <div className="art-card art-card-score"><Icon name="sparkles" size={17}/><div><small>{t("auth.artAiLabel")}</small><strong>{t("auth.artAiValue")}</strong></div></div>
      </div>
      <p className="brand-foot"><Icon name="shield" size={16}/> {t("auth.brandFoot")}</p>
    </section>
    <section className="login-form-side">
      <div className="mobile-login-logo"><Logo/></div>
      <div className="login-form-wrap">
        <div className="form-heading"><h2>{t("auth.welcome")}</h2><p>{t("auth.subtitle")}</p></div>
        <button className="primary-button login-submit" onClick={() => login(from)}>{t("auth.signIn")} <Icon name="arrow" size={17}/></button>
        <div className="divider"><span>{t("auth.newHere")}</span></div>
        <button className="sso-button" onClick={() => register()}><Icon name="users" size={17}/>{t("auth.createAccount")}</button>
        <p className="form-help"><Icon name="lock" size={13}/> {t("auth.secureNote")}</p>
      </div>
      <div className="login-footer">
        <span>{t("auth.footerCopyright", { year: new Date().getFullYear() })}</span><span>{t("auth.footerPrivacy")}</span><span>{t("auth.footerImprint")}</span>
        <button className="link-button" onClick={() => changeLanguage(currentLanguage() === "en" ? "de" : "en")}>{t("auth.switchLanguage")}</button>
      </div>
    </section>
  </main>;
}
