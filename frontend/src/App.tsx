import { Navigate, Route, Routes } from "react-router";
import AppShell from "@/components/layout/AppShell";
import LoginPage from "@/features/auth/pages/LoginPage";
import CandidateDetailPage from "@/features/candidates/pages/CandidateDetailPage";
import DashboardPage from "@/features/dashboard/pages/DashboardPage";
import CreateJobPage from "@/features/jobs/pages/CreateJobPage";
import JobsPage from "@/features/jobs/pages/JobsPage";
import PipelinePage from "@/features/pipeline/pages/PipelinePage";

export default function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage/>}/>
    <Route element={<AppShell/>}>
      <Route path="/dashboard" element={<DashboardPage/>}/>
      <Route path="/jobs" element={<JobsPage/>}/>
      <Route path="/jobs/new" element={<CreateJobPage/>}/>
      <Route path="/pipeline" element={<PipelinePage/>}/>
      <Route path="/candidates/:id" element={<CandidateDetailPage/>}/>
    </Route>
    <Route path="*" element={<Navigate to="/dashboard" replace/>}/>
  </Routes>;
}
