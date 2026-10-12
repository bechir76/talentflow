import type { IconName } from "@/components/common/Icon";

export type NavItem = {
  icon: IconName;
  label: string;
  to: string;
  badge?: string;
  /** Path prefixes that mark this item as active. Items without it are never highlighted. */
  activeFor?: string[];
};

const recruiterNav: NavItem[] = [
  { icon: "grid", label: "Overview", to: "/dashboard", activeFor: ["/dashboard"] },
  { icon: "briefcase", label: "Jobs", to: "/jobs", badge: "12", activeFor: ["/jobs"] },
  { icon: "users", label: "Candidates", to: "/pipeline", activeFor: ["/pipeline", "/candidates"] },
  { icon: "calendar", label: "Interviews", to: "/dashboard", badge: "9" },
  { icon: "chart", label: "Reports", to: "/dashboard" },
];

export const roleNav: Record<string, NavItem[]> = {
  Recruiter: recruiterNav,
  "Hiring Manager": [
    { icon: "grid", label: "Overview", to: "/dashboard", activeFor: ["/dashboard"] },
    { icon: "briefcase", label: "My Openings", to: "/jobs", badge: "4", activeFor: ["/jobs"] },
    { icon: "users", label: "Shortlists", to: "/pipeline", activeFor: ["/pipeline", "/candidates"] },
    { icon: "calendar", label: "Interviews", to: "/dashboard", badge: "3" },
  ],
  Admin: [
    { icon: "grid", label: "Overview", to: "/dashboard", activeFor: ["/dashboard"] },
    { icon: "users", label: "Team", to: "/dashboard" },
    { icon: "briefcase", label: "All Jobs", to: "/jobs", badge: "12", activeFor: ["/jobs"] },
    { icon: "chart", label: "Analytics", to: "/dashboard" },
    { icon: "shield", label: "Audit Log", to: "/dashboard" },
  ],
  Candidate: [
    { icon: "grid", label: "My Overview", to: "/dashboard", activeFor: ["/dashboard"] },
    { icon: "briefcase", label: "Find Jobs", to: "/jobs", activeFor: ["/jobs"] },
    { icon: "chart", label: "Applications", to: "/dashboard", badge: "3" },
    { icon: "calendar", label: "My Interviews", to: "/dashboard", badge: "1" },
  ],
};

export function navFor(role: string): NavItem[] {
  return roleNav[role] ?? recruiterNav;
}

export function isActive(item: NavItem, pathname: string): boolean {
  return item.activeFor?.some((prefix) => pathname.startsWith(prefix)) ?? false;
}
