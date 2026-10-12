import type { ParseKeys } from "i18next";
import type { IconName } from "@/components/common/Icon";
import type { Role } from "@/lib/auth/roles";

export type NavItem = {
  icon: IconName;
  labelKey: ParseKeys;
  to: string;
  /** Path prefixes that mark this item as active. */
  activeFor: string[];
};

const overview: NavItem = { icon: "grid", labelKey: "nav.overview", to: "/dashboard", activeFor: ["/dashboard"] };
const interviewsItem: NavItem = { icon: "calendar", labelKey: "nav.interviews", to: "/interviews", activeFor: ["/interviews"] };

/** Sidebar entries per role. Only screens that exist are listed; later phases add more. */
const navByRole: Record<Role, NavItem[]> = {
  RECRUITER: [
    overview,
    { icon: "briefcase", labelKey: "nav.jobs", to: "/jobs", activeFor: ["/jobs"] },
    { icon: "users", labelKey: "nav.candidates", to: "/pipeline", activeFor: ["/pipeline", "/applications"] },
    interviewsItem,
  ],
  HIRING_MANAGER: [
    overview,
    { ...interviewsItem, activeFor: ["/interviews", "/applications"] },
  ],
  ADMIN: [
    overview,
    { icon: "briefcase", labelKey: "nav.allJobs", to: "/jobs", activeFor: ["/jobs"] },
    { icon: "users", labelKey: "nav.candidates", to: "/pipeline", activeFor: ["/pipeline", "/applications"] },
    interviewsItem,
    { icon: "settings", labelKey: "nav.users", to: "/admin/users", activeFor: ["/admin/users"] },
    { icon: "clock", labelKey: "nav.retention", to: "/admin/retention", activeFor: ["/admin/retention"] },
    { icon: "shield", labelKey: "nav.auditLog", to: "/admin/audit", activeFor: ["/admin/audit"] },
  ],
  CANDIDATE: [
    { icon: "file", labelKey: "nav.myApplications", to: "/portal/applications", activeFor: ["/portal/applications"] },
    { icon: "briefcase", labelKey: "nav.findJobs", to: "/careers", activeFor: ["/careers"] },
    { icon: "shield", labelKey: "nav.privacy", to: "/portal/privacy", activeFor: ["/portal/privacy"] },
  ],
};

export function navFor(role: Role | undefined): NavItem[] {
  return role ? navByRole[role] : [];
}

export function isActive(item: NavItem, pathname: string): boolean {
  return item.activeFor.some((prefix) => pathname.startsWith(prefix));
}
