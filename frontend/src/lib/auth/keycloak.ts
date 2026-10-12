import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8180",
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? "talentflow",
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "talentflow-frontend",
});

let initialization: Promise<boolean> | undefined;

/**
 * Initialises Keycloak exactly once (React StrictMode mounts effects twice).
 * Uses the authorization code flow with PKCE and a silent SSO check.
 */
export function initKeycloak(): Promise<boolean> {
  initialization ??= keycloak.init({
    onLoad: "check-sso",
    pkceMethod: "S256",
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    checkLoginIframe: false,
  });
  return initialization;
}
