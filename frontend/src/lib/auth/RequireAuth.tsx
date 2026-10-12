import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "./AuthContext";
import { homePathFor, type Role } from "./roles";

/**
 * Renders child routes only for signed-in users that hold one of `roles` (any role if omitted).
 * Anonymous users go to the login page; users without the role go to their own home page.
 */
export default function RequireAuth({ roles }: { roles?: Role[] }) {
  const { state } = useAuth();
  const location = useLocation();

  if (state.status !== "authenticated") {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }}/>;
  }
  const { user } = state;
  if (!user.primaryRole) {
    return <Navigate to="/no-access" replace/>;
  }
  if (roles && !roles.some((role) => user.roles.includes(role))) {
    return <Navigate to={homePathFor(user.primaryRole)} replace/>;
  }
  return <Outlet/>;
}
