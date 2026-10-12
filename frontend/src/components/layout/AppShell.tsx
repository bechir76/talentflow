import { useState } from "react";
import { Outlet } from "react-router";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";

export default function AppShell() {
  const [collapsed, setCollapsed] = useState(false);
  const [dark, setDark] = useState(false);
  return <div className={`app-shell ${dark ? "dark" : ""}`}>
    <Sidebar collapsed={collapsed} setCollapsed={setCollapsed}/>
    <div className="app-main">
      <Topbar dark={dark} setDark={setDark}/>
      <Outlet/>
    </div>
  </div>;
}
