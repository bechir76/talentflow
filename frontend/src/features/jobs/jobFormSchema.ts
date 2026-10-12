import { z } from "zod";

export const MAX_SKILLS = 30;
export const DESCRIPTION_MIN = 50;
export const DESCRIPTION_MAX = 20000;

// Error messages are i18n keys under "validation.".
const optionalSalary = z.number({ error: "number" }).int("number").nonnegative("nonNegative").optional();

export const jobFormSchema = z
  .object({
    title: z.string().trim().min(1, "required").max(150, "tooLong"),
    departmentId: z.string().min(1, "required"),
    location: z.string().trim().min(1, "required").max(150, "tooLong"),
    workMode: z.enum(["REMOTE", "HYBRID", "ONSITE"], { error: "required" }),
    salaryMin: optionalSalary,
    salaryMax: optionalSalary,
    requiredSkills: z.array(z.string()).min(1, "skillsRequired").max(MAX_SKILLS, "tooManySkills"),
    description: z.string().trim().min(DESCRIPTION_MIN, "descriptionTooShort").max(DESCRIPTION_MAX, "tooLong"),
  })
  .refine((job) => job.salaryMin == null || job.salaryMax == null || job.salaryMin <= job.salaryMax, {
    path: ["salaryMax"],
    message: "salaryRange",
  });

export type JobFormValues = z.infer<typeof jobFormSchema>;
