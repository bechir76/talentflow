import { useTranslation } from "react-i18next";
import { Navigate, Route, Routes } from "react-router";
import AppShell from "@/components/layout/AppShell";
import ApplyPage from "@/features/applications/pages/ApplyPage";
import AuthStatusScreen from "@/features/auth/components/AuthStatusScreen";
import LoginPage from "@/features/auth/pages/LoginPage";
import NoAccessPage from "@/features/auth/pages/NoAccessPage";
import PublicLayout from "@/features/careers/components/PublicLayout";
import CareerJobPage from "@/features/careers/pages/CareerJobPage";
import CareersPage from "@/features/careers/pages/CareersPage";
import CandidateDetailPage from "@/features/candidates/pages/CandidateDetailPage";
import DashboardPage from "@/features/dashboard/pages/DashboardPage";
import JobFormPage from "@/features/jobs/pages/JobFormPage";
import JobsPage from "@/features/jobs/pages/JobsPage";
import PipelinePage from "@/features/pipeline/pages/PipelinePage";
import MyApplicationsPage from "@/features/portal/pages/MyApplicationsPage";
import { useAuth } from "@/lib/auth/AuthContext";
import RequireAuth from "@/lib/auth/RequireAuth";
import { homePathFor, STAFF_ROLES } from "@/lib/auth/roles";

function HomeRedirect() {
  const { state } = useAuth();
  if (state.status !== "authenticated") return <Navigate to="/login" replace/>;
  return <Navigate to={homePathFor(state.user.primaryRole)} replace/>;
}

export default function App() {
  const { t } = useTranslation();
  const { state } = useAuth();

  if (state.status === "loading") {
    return <AuthStatusScreen message={t("auth.connecting")}/>;
  }
  if (state.status === "error") {
    return <AuthStatusScreen
      title={t("auth.unavailableTitle")}
      message={t("auth.unavailableText")}
      action={<button className="primary-button" onClick={() => window.location.reload()}>{t("common.retry")}</button>}
    />;
  }

  return <Routes>
    <Route path="/login" element={<LoginPage/>}/>
    <Route path="/no-access" element={<NoAccessPage/>}/>
    <Route element={<PublicLayout/>}>
      <Route path="/careers" element={<CareersPage/>}/>
      <Route path="/careers/:id" element={<CareerJobPage/>}/>
      <Route path="/careers/:id/apply" element={<ApplyPage/>}/>
    </Route>
    <Route element={<RequireAuth/>}>
      <Route element={<AppShell/>}>
        <Route element={<RequireAuth roles={STAFF_ROLES}/>}>
          <Route path="/dashboard" element={<DashboardPage/>}/>
          <Route path="/pipeline" element={<PipelinePage/>}/>
          <Route path="/candidates/:id" element={<CandidateDetailPage/>}/>
        </Route>
        <Route element={<RequireAuth roles={["RECRUITER", "ADMIN"]}/>}>
          <Route path="/jobs" element={<JobsPage/>}/>
          <Route path="/jobs/new" element={<JobFormPage/>}/>
          <Route path="/jobs/:id/edit" element={<JobFormPage/>}/>
        </Route>
        <Route element={<RequireAuth roles={["CANDIDATE"]}/>}>
          <Route path="/portal/applications" element={<MyApplicationsPage/>}/>
        </Route>
      </Route>
    </Route>
    <Route path="*" element={<HomeRedirect/>}/>
  </Routes>;
}
