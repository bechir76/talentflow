import { useState } from "react";
import { useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";

export default function Topbar({ dark, setDark }: { dark: boolean; setDark: (value: boolean) => void }) {
  const navigate = useNavigate();
  const [language, setLanguage] = useState("EN");
  return <header className="topbar">
    <div className="global-search"><Icon name="search"/><input placeholder="Search candidates, jobs, interviews..."/><kbd>⌘ K</kbd></div>
    <div className="top-actions">
      <button className="icon-button theme-button" onClick={() => setDark(!dark)} aria-label="Toggle dark mode"><Icon name={dark ? "sun" : "moon"}/></button>
      <button className="language-button" onClick={() => setLanguage(language === "EN" ? "DE" : "EN")}><Icon name="globe" size={16}/>{language}</button>
      <button className="icon-button notification"><Icon name="bell"/><i></i></button>
      <span className="top-divider"></span>
      <button className="profile-button" onClick={() => navigate("/login")}><Avatar initials="AK" color="violet" small/><span><strong>Anna Keller</strong><small>Recruiter</small></span><Icon name="chevron" size={14}/></button>
    </div>
  </header>;
}
