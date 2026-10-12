import { describe, expect, it } from "vitest";
import { homePathFor, isRole, primaryRole } from "./roles";

describe("roles", () => {
  it("picks the most powerful role as the primary role", () => {
    expect(primaryRole(["CANDIDATE", "RECRUITER"])).toBe("RECRUITER");
    expect(primaryRole(["HIRING_MANAGER", "ADMIN"])).toBe("ADMIN");
    expect(primaryRole([])).toBeUndefined();
  });

  it("sends every role to its own home page", () => {
    expect(homePathFor("CANDIDATE")).toBe("/portal/applications");
    expect(homePathFor("HIRING_MANAGER")).toBe("/dashboard");
    expect(homePathFor(undefined)).toBe("/no-access");
  });

  it("ignores Keycloak built-in roles", () => {
    expect(isRole("offline_access")).toBe(false);
    expect(isRole("ADMIN")).toBe(true);
  });
});
