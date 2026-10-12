// Placeholder data until the dashboard endpoints exist.
export const topMatches = [
  { id: "lena-schmidt", name: "Lena Schmidt", initials: "LS", role: "Senior Backend Engineer", score: 94, color: "violet" },
  { id: "amira-haddad", name: "Amira Haddad", initials: "AH", role: "Java Software Engineer", score: 92, color: "rose" },
  { id: "jonas-weber", name: "Jonas Weber", initials: "JW", role: "Cloud Platform Engineer", score: 89, color: "blue" },
];

export const applicationsPerJob: [name: string, heightPercent: number, count: string][] = [
  ["Java Dev.", 86, "112"], ["Product Des.", 62, "81"], ["Sales Mgr.", 47, "64"],
  ["Data Analyst", 72, "93"], ["People Ops", 34, "46"], ["DevOps Eng.", 55, "72"],
];

export const funnelSteps: [name: string, count: string, rate: string, widthClass: string][] = [
  ["Applied", "428", "100%", "funnel-1"], ["Screening", "196", "46%", "funnel-2"],
  ["Interview", "84", "43%", "funnel-3"], ["Offer", "31", "37%", "funnel-4"], ["Hired", "18", "58%", "funnel-5"],
];

export const todaysInterviews: [time: string, name: string, type: string, interviewer: string][] = [
  ["10:00", "Lena Schmidt", "Technical interview", "MR"],
  ["13:30", "Amira Haddad", "Culture add interview", "JW"],
  ["16:00", "Felix Wagner", "Hiring manager call", "AK"],
];
