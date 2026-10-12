import { describe, expect, it } from "vitest";
import { applyFormSchema, MAX_CV_BYTES } from "./applyFormSchema";

const pdf = (size = 1000, name = "cv.pdf", type = "application/pdf") => {
  const file = new File(["%PDF-1.7"], name, { type });
  Object.defineProperty(file, "size", { value: size });
  return file;
};

const firstError = (input: unknown) => {
  const result = applyFormSchema.safeParse(input);
  return result.success ? undefined : result.error.issues[0]?.message;
};

describe("applyFormSchema", () => {
  it("accepts a PDF with consent", () => {
    expect(applyFormSchema.safeParse({ cv: pdf(), consent: true }).success).toBe(true);
  });

  it("requires explicit consent (GDPR)", () => {
    expect(firstError({ cv: pdf(), consent: false })).toBe("consentRequired");
  });

  it("requires a PDF of at most 5 MB", () => {
    expect(firstError({ consent: true })).toBe("cvRequired");
    expect(firstError({ cv: pdf(1000, "cv.docx", "application/msword"), consent: true })).toBe("cvPdfOnly");
    expect(firstError({ cv: pdf(MAX_CV_BYTES + 1), consent: true })).toBe("cvTooLarge");
  });
});
