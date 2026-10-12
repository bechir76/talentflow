import { createContext, useContext } from "react";
import type { Role } from "./roles";

export type AuthUser = {
  id: string;
  email?: string;
  fullName: string;
  firstName?: string;
  initials: string;
  roles: Role[];
  primaryRole?: Role;
};

export type AuthState =
  | { status: "loading" }
  | { status: "error" }
  | { status: "anonymous" }
  | { status: "authenticated"; user: AuthUser };

export type AuthContextValue = {
  state: AuthState;
  /** Redirects to the identity provider; returns to `returnPath` afterwards. */
  login: (returnPath?: string) => void;
  /** Opens the identity provider's self-registration page; returns to `returnPath` afterwards. */
  register: (returnPath?: string) => void;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside <AuthProvider>");
  return context;
}

/** Returns the signed-in user. Only use below a route guard. */
export function useCurrentUser(): AuthUser {
  const { state } = useAuth();
  if (state.status !== "authenticated") throw new Error("useCurrentUser requires an authenticated user");
  return state.user;
}
