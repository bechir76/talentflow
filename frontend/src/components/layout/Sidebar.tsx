import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import Logo from "@/components/common/Logo";
import { useCurrentUser } from "@/lib/auth/AuthContext";
import { isActive, navFor } from "./navigation";

type SidebarProps = {
  collapsed: boolean;
  setCollapsed: (value: boolean) => void;
};

export default function Sidebar({ collapsed, setCollapsed }: SidebarProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const user = useCurrentUser();
  const nav = navFor(user.primaryRole);
  const isStaff = user.primaryRole !== "CANDIDATE";
  return <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
    <div className="sidebar-head"><Logo compact={collapsed}/><button className="collapse-button" onClick={() => setCollapsed(!collapsed)} aria-label={t("shell.toggleSidebar")}><Icon name="menu"/></button></div>
    {isStaff && <div className="workspace-switch"><span className="workspace-logo">AG</span>{!collapsed && <><div><strong>Acme GmbH</strong><small>Recruiting</small></div><Icon name="chevron" size={14}/></>}</div>}
    <nav className="nav-list">
      {!collapsed && <span className="nav-label">{t("shell.workspace")}</span>}
      {nav.map((item) => <button key={item.to} className={isActive(item, pathname) ? "active" : ""} onClick={() => navigate(item.to)}>
        <Icon name={item.icon}/>{!collapsed && <span>{t(item.labelKey)}</span>}
      </button>)}
    </nav>
    <div className="sidebar-bottom">
      {isStaff && <>
        {!collapsed && <span className="nav-label">{t("shell.manage")}</span>}
        <button><Icon name="settings"/>{!collapsed && <span>{t("shell.settings")}</span>}</button>
        <button><Icon name="shield"/>{!collapsed && <span>{t("shell.compliance")}</span>}</button>
      </>}
      <div className="role-switch">
        <Avatar initials={user.initials} color="violet" small/>
        {!collapsed && <div><strong>{user.fullName}</strong>{user.primaryRole && <small>{t(`roles.${user.primaryRole}`)}</small>}</div>}
      </div>
    </div>
  </aside>;
}
