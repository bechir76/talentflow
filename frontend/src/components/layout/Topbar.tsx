import { useTranslation } from "react-i18next";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import { useMe } from "@/features/auth/api/useMe";
import { useAuth, useCurrentUser } from "@/lib/auth/AuthContext";
import { changeLanguage, currentLanguage } from "@/lib/i18n";

export default function Topbar({ dark, setDark }: { dark: boolean; setDark: (value: boolean) => void }) {
  const { t } = useTranslation();
  const { logout } = useAuth();
  const user = useCurrentUser();
  const { data: me } = useMe();
  const displayName = me?.fullName ?? user.fullName;
  return <header className="topbar">
    <span></span>
    <div className="top-actions">
      <button className="icon-button theme-button" onClick={() => setDark(!dark)} aria-label={t("shell.toggleDarkMode")}><Icon name={dark ? "sun" : "moon"}/></button>
      <button className="language-button" onClick={() => changeLanguage(currentLanguage() === "en" ? "de" : "en")}><Icon name="globe" size={16}/>{t("shell.languageLabel")}</button>
      <span className="top-divider"></span>
      <div className="profile-button"><Avatar initials={user.initials} color="violet" small/><span><strong>{displayName}</strong>{user.primaryRole && <small>{t(`roles.${user.primaryRole}`)}</small>}</span></div>
      <button className="icon-button" onClick={logout} aria-label={t("shell.signOut")} title={t("shell.signOut")}><Icon name="logout"/></button>
    </div>
  </header>;
}
