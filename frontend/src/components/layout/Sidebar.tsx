import { useLocation, useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import Logo from "@/components/common/Logo";
import { isActive, navFor } from "./navigation";

type SidebarProps = {
  collapsed: boolean;
  setCollapsed: (value: boolean) => void;
  role: string;
  setRole: (role: string) => void;
};

export default function Sidebar({ collapsed, setCollapsed, role, setRole }: SidebarProps) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const nav = navFor(role);
  return <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
    <div className="sidebar-head"><Logo compact={collapsed}/><button className="collapse-button" onClick={() => setCollapsed(!collapsed)}><Icon name="menu"/></button></div>
    <div className="workspace-switch"><span className="workspace-logo">AK</span>{!collapsed && <><div><strong>Acme GmbH</strong><small>Recruiting</small></div><Icon name="chevron" size={14}/></>}</div>
    <nav className="nav-list">
      {!collapsed && <span className="nav-label">Workspace</span>}
      {nav.map((item) => <button key={item.label} className={isActive(item, pathname) ? "active" : ""} onClick={() => navigate(item.to)}>
        <Icon name={item.icon}/>{!collapsed && <><span>{item.label}</span>{item.badge && <b>{item.badge}</b>}</>}
      </button>)}
    </nav>
    <div className="sidebar-bottom">
      {!collapsed && <span className="nav-label">Manage</span>}
      <button><Icon name="settings"/>{!collapsed && <span>Settings</span>}</button>
      <button><Icon name="shield"/>{!collapsed && <span>Compliance</span>}</button>
      <div className="role-switch">
        <Avatar initials="AK" color="violet" small/>
        {!collapsed && <div><strong>Anna Keller</strong><select value={role} onChange={(e) => setRole(e.target.value)} aria-label="Select role"><option>Recruiter</option><option>Hiring Manager</option><option>Admin</option><option>Candidate</option></select></div>}
      </div>
    </div>
  </aside>;
}
