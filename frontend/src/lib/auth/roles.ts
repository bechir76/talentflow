export const ROLES = ["ADMIN", "RECRUITER", "HIRING_MANAGER", "CANDIDATE"] as const;
export type Role = (typeof ROLES)[number];

/** Staff roles that work inside the recruiting workspace. */
export const STAFF_ROLES: Role[] = ["ADMIN", "RECRUITER", "HIRING_MANAGER"];

export function isRole(value: string): value is Role {
  return (ROLES as readonly string[]).includes(value);
}

/** The role that decides the user's home page and menu. ROLES is ordered by priority. */
export function primaryRole(roles: Role[]): Role | undefined {
  return ROLES.find((role) => roles.includes(role));
}

export function homePathFor(role: Role | undefined): string {
  if (role === "CANDIDATE") return "/portal/applications";
  if (role) return "/dashboard";
  return "/no-access";
}
