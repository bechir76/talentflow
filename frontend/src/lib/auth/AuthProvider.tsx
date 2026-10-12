import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import type { KeycloakTokenParsed } from "keycloak-js";
import { setTokenProvider } from "@/lib/api/client";
import { currentLanguage } from "@/lib/i18n";
import { AuthContext, type AuthState, type AuthUser } from "./AuthContext";
import { initKeycloak, keycloak } from "./keycloak";
import { isRole, primaryRole } from "./roles";

/** Refresh the access token when it expires within this many seconds. */
const MIN_TOKEN_VALIDITY_SECONDS = 30;

type TalentflowToken = KeycloakTokenParsed & {
  name?: string;
  given_name?: string;
  email?: string;
};

function toUser(token: TalentflowToken): AuthUser {
  const roles = (token.realm_access?.roles ?? []).filter(isRole);
  const fullName = token.name ?? token.email ?? "";
  const initials = fullName.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]!.toUpperCase()).join("");
  return {
    id: token.sub ?? "",
    email: token.email,
    fullName,
    firstName: token.given_name,
    initials: initials || "?",
    roles,
    primaryRole: primaryRole(roles),
  };
}

function stateFromKeycloak(): AuthState {
  return keycloak.authenticated && keycloak.tokenParsed
    ? { status: "authenticated", user: toUser(keycloak.tokenParsed as TalentflowToken) }
    : { status: "anonymous" };
}

async function freshToken(): Promise<string | undefined> {
  if (!keycloak.authenticated) return undefined;
  try {
    await keycloak.updateToken(MIN_TOKEN_VALIDITY_SECONDS);
    return keycloak.token;
  } catch {
    // The session ended (for example logged out elsewhere); force a new login.
    keycloak.clearToken();
    return undefined;
  }
}

export default function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: "loading" });

  useEffect(() => {
    let active = true;
    keycloak.onAuthRefreshSuccess = () => active && setState(stateFromKeycloak());
    keycloak.onAuthLogout = () => active && setState({ status: "anonymous" });
    keycloak.onTokenExpired = () => void freshToken();
    setTokenProvider(freshToken);
    initKeycloak()
      .then(() => active && setState(stateFromKeycloak()))
      .catch(() => active && setState({ status: "error" }));
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback((returnPath = "/") => {
    void keycloak.login({ redirectUri: window.location.origin + returnPath, locale: currentLanguage() });
  }, []);

  const register = useCallback((returnPath = "/") => {
    void keycloak.register({ redirectUri: window.location.origin + returnPath, locale: currentLanguage() });
  }, []);

  const logout = useCallback(() => {
    void keycloak.logout({ redirectUri: window.location.origin + "/login" });
  }, []);

  const value = useMemo(() => ({ state, login, register, logout }), [state, login, register, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
