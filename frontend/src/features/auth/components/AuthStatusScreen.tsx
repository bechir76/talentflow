import type { ReactNode } from "react";
import Logo from "@/components/common/Logo";

type AuthStatusScreenProps = {
  title?: string;
  message: string;
  action?: ReactNode;
};

/** Full-page message shown while connecting to, or failing to reach, the identity provider. */
export default function AuthStatusScreen({ title, message, action }: AuthStatusScreenProps) {
  return <main className="auth-status" aria-live="polite">
    <div className="auth-status-card panel">
      <Logo/>
      {title && <h2>{title}</h2>}
      <p>{message}</p>
      {action}
    </div>
  </main>;
}
