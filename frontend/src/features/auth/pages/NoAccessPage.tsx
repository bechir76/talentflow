import { useTranslation } from "react-i18next";
import { Navigate } from "react-router";
import Icon from "@/components/common/Icon";
import { useAuth } from "@/lib/auth/AuthContext";
import { homePathFor } from "@/lib/auth/roles";
import AuthStatusScreen from "../components/AuthStatusScreen";

export default function NoAccessPage() {
  const { t } = useTranslation();
  const { state, logout } = useAuth();
  if (state.status !== "authenticated") return <Navigate to="/login" replace/>;
  if (state.user.primaryRole) return <Navigate to={homePathFor(state.user.primaryRole)} replace/>;
  return <AuthStatusScreen
    title={t("auth.noAccessTitle")}
    message={t("auth.noAccessText")}
    action={<button className="secondary-button" onClick={logout}><Icon name="logout" size={14}/>{t("shell.signOut")}</button>}
  />;
}
