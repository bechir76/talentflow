import { describe, expect, it } from "vitest";
import { jobFormSchema } from "./jobFormSchema";

const valid = {
  title: "Senior Java Developer",
  departmentId: "dep-1",
  location: "Berlin",
  workMode: "HYBRID" as const,
  salaryMin: 70000,
  salaryMax: 90000,
  requiredSkills: ["Java"],
  description: "We are looking for an engineer who builds reliable backend services.",
};

const errorsOf = (input: unknown) => {
  const result = jobFormSchema.safeParse(input);
  return result.success ? {} : Object.fromEntries(result.error.issues.map((issue) => [issue.path.join("."), issue.message]));
};

describe("jobFormSchema", () => {
  it("accepts a complete job", () => {
    expect(jobFormSchema.safeParse(valid).success).toBe(true);
  });

  it("reports missing fields with i18n keys", () => {
    expect(errorsOf({ ...valid, title: "  ", requiredSkills: [], description: "short" })).toEqual({
      title: "required",
      requiredSkills: "skillsRequired",
      description: "descriptionTooShort",
    });
  });

  it("rejects a minimum salary above the maximum", () => {
    expect(errorsOf({ ...valid, salaryMin: 100000 })).toEqual({ salaryMax: "salaryRange" });
  });

  it("allows leaving the salary out", () => {
    expect(jobFormSchema.safeParse({ ...valid, salaryMin: undefined, salaryMax: undefined }).success).toBe(true);
  });
});
