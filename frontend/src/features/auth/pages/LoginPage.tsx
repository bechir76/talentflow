import { useState } from "react";
import { useNavigate } from "react-router";
import Avatar from "@/components/common/Avatar";
import Icon from "@/components/common/Icon";
import Logo from "@/components/common/Logo";

export default function LoginPage() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const onLogin = () => navigate("/dashboard");
  return <main className="login-page">
    <section className="login-brand">
      <Logo />
      <div className="login-copy">
        <span className="eyebrow light"><Icon name="sparkles" size={15}/> Built for modern European teams</span>
        <h1>Hire smarter.<br/>Stay compliant.</h1>
        <p>AI-assisted recruiting that helps you find exceptional people while keeping privacy at the heart of every decision.</p>
      </div>
      <div className="login-art" aria-hidden="true">
        <div className="art-orbit orbit-one"></div><div className="art-orbit orbit-two"></div>
        <div className="art-card art-card-main">
          <div className="art-title"><span className="mini-logo">T</span><span>Candidate match</span><b>94%</b></div>
          <div className="art-profile"><Avatar initials="LS" color="rose"/><div><strong>Lena Schmidt</strong><small>Senior Backend Engineer</small></div></div>
          <div className="art-bars"><i></i><i></i><i></i></div>
          <div className="art-tags"><span>Java</span><span>Spring Boot</span><span>Docker</span></div>
        </div>
        <div className="art-card art-card-small"><span className="success-dot"><Icon name="check" size={13}/></span><div><strong>GDPR ready</strong><small>Data processed in EU</small></div></div>
        <div className="art-card art-card-score"><Icon name="sparkles" size={17}/><div><small>AI screening</small><strong>18h saved</strong></div></div>
      </div>
      <p className="brand-foot"><Icon name="shield" size={16}/> GDPR compliant · Hosted in the EU · ISO 27001</p>
    </section>
    <section className="login-form-side">
      <div className="mobile-login-logo"><Logo/></div>
      <div className="login-form-wrap">
        <div className="form-heading"><h2>Welcome back</h2><p>Sign in to continue to your workspace.</p></div>
        <button className="sso-button" onClick={onLogin}><span className="sso-icon">S</span>Sign in with SSO</button>
        <div className="divider"><span>or continue with email</span></div>
        <form onSubmit={(event) => { event.preventDefault(); onLogin(); }}>
          <label>Email address<input type="email" defaultValue="anna.keller@talentflow.eu"/></label>
          <label>Password<div className="password-wrap"><input type={showPassword ? "text" : "password"} defaultValue="talentflow"/><button type="button" className="field-icon" onClick={() => setShowPassword(!showPassword)}><Icon name="eye" size={18}/></button></div></label>
          <div className="form-options"><label className="checkbox-label"><input type="checkbox" defaultChecked/>Keep me signed in</label><button type="button" className="link-button">Forgot password?</button></div>
          <button className="primary-button login-submit">Sign in <Icon name="arrow" size={17}/></button>
        </form>
        <p className="form-help">Having trouble? <button className="link-button">Contact support</button></p>
      </div>
      <div className="login-footer"><span>© 2025 TalentFlow GmbH</span><span>Privacy</span><span>Imprint</span><span>Deutsch</span></div>
    </section>
  </main>;
}
