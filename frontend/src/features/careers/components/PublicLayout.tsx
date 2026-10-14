import { useTranslation } from "react-i18next";
import { Link, Outlet, useLocation, useNavigate } from "react-router";
import Icon from "@/components/common/Icon";
import Logo from "@/components/common/Logo";
import { useAuth } from "@/lib/auth/AuthContext";
import { homePathFor } from "@/lib/auth/roles";
import { changeLanguage, currentLanguage } from "@/lib/i18n";

/** Header and footer for pages that do not require sign-in (career page, job details). */
export default function PublicLayout() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { state, login } = useAuth();
  const signedIn = state.status === "authenticated";

  return <div className="public-shell">
    <header className="public-header">
      <Link to="/careers" className="public-logo" aria-label={t("careers.title")}><Logo/></Link>
      <nav className="public-actions">
        <button className="language-button" onClick={() => changeLanguage(currentLanguage() === "en" ? "de" : "en")}><Icon name="globe" size={16}/>{t("shell.languageLabel")}</button>
        {signedIn
          ? <button className="secondary-button" onClick={() => navigate(homePathFor(state.user.primaryRole))}>{t(state.user.primaryRole === "CANDIDATE" ? "nav.myApplications" : "careers.openWorkspace")}</button>
          : <button className="secondary-button" onClick={() => login(location.pathname)}>{t("auth.signIn")}</button>}
      </nav>
    </header>
    <main className="public-main"><Outlet/></main>
    <footer className="public-footer">
      <span>{t("auth.footerCopyright", { year: new Date().getFullYear() })}</span><Link to="/privacy">{t("auth.footerPrivacy")}</Link>
    </footer>
  </div>;
}
