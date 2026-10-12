import { lazy, Suspense } from "react";
import { useTranslation } from "react-i18next";
import { Navigate, Route, Routes } from "react-router";
import AppShell from "@/components/layout/AppShell";
import AuthStatusScreen from "@/features/auth/components/AuthStatusScreen";
import LoginPage from "@/features/auth/pages/LoginPage";
import NoAccessPage from "@/features/auth/pages/NoAccessPage";
import PublicLayout from "@/features/careers/components/PublicLayout";
import { useAuth } from "@/lib/auth/AuthContext";
import RequireAuth from "@/lib/auth/RequireAuth";
import { homePathFor, STAFF_ROLES } from "@/lib/auth/roles";

// Pages are loaded on demand, so each role only downloads the screens it uses.
const ApplyPage = lazy(() => import("@/features/applications/pages/ApplyPage"));
const CareerJobPage = lazy(() => import("@/features/careers/pages/CareerJobPage"));
const CareersPage = lazy(() => import("@/features/careers/pages/CareersPage"));
const CandidateDetailPage = lazy(() => import("@/features/candidates/pages/CandidateDetailPage"));
const DashboardPage = lazy(() => import("@/features/dashboard/pages/DashboardPage"));
const JobFormPage = lazy(() => import("@/features/jobs/pages/JobFormPage"));
const JobsPage = lazy(() => import("@/features/jobs/pages/JobsPage"));
const CalendarPage = lazy(() => import("@/features/interviews/pages/CalendarPage"));
const PipelinePage = lazy(() => import("@/features/pipeline/pages/PipelinePage"));
const MyApplicationsPage = lazy(() => import("@/features/portal/pages/MyApplicationsPage"));
const PrivacyPage = lazy(() => import("@/features/portal/pages/PrivacyPage"));
const AuditLogPage = lazy(() => import("@/features/admin/pages/AuditLogPage"));
const RetentionPage = lazy(() => import("@/features/admin/pages/RetentionPage"));
const UsersPage = lazy(() => import("@/features/admin/pages/UsersPage"));

function HomeRedirect() {
  const { state } = useAuth();
  if (state.status !== "authenticated") return <Navigate to="/login" replace/>;
  return <Navigate to={homePathFor(state.user.primaryRole)} replace/>;
}

function PageLoading() {
  const { t } = useTranslation();
  return <div className="table-empty page-loading"><span className="spinner"></span><strong>{t("common.loading")}</strong></div>;
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

  return <Suspense fallback={<PageLoading/>}>
    <Routes>
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
            <Route path="/applications/:id" element={<CandidateDetailPage/>}/>
            <Route path="/interviews" element={<CalendarPage/>}/>
          </Route>
          <Route element={<RequireAuth roles={["RECRUITER", "ADMIN"]}/>}>
            <Route path="/pipeline" element={<PipelinePage/>}/>
            <Route path="/jobs" element={<JobsPage/>}/>
            <Route path="/jobs/new" element={<JobFormPage/>}/>
            <Route path="/jobs/:id/edit" element={<JobFormPage/>}/>
          </Route>
          <Route element={<RequireAuth roles={["CANDIDATE"]}/>}>
            <Route path="/portal/applications" element={<MyApplicationsPage/>}/>
            <Route path="/portal/privacy" element={<PrivacyPage/>}/>
          </Route>
          <Route element={<RequireAuth roles={["ADMIN"]}/>}>
            <Route path="/admin/users" element={<UsersPage/>}/>
            <Route path="/admin/retention" element={<RetentionPage/>}/>
            <Route path="/admin/audit" element={<AuditLogPage/>}/>
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<HomeRedirect/>}/>
    </Routes>
  </Suspense>;
}
