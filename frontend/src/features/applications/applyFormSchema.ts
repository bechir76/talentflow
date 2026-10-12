import { z } from "zod";

export const MAX_CV_BYTES = 5 * 1024 * 1024;
export const MAX_COVER_LETTER = 5000;

// Error messages are i18n keys under "validation.".
export const applyFormSchema = z.object({
  cv: z
    .instanceof(File, { error: "cvRequired" })
    .refine((file) => file.type === "application/pdf" || file.name.toLowerCase().endsWith(".pdf"), "cvPdfOnly")
    .refine((file) => file.size <= MAX_CV_BYTES, "cvTooLarge"),
  phone: z.string().trim().max(40, "tooLong").optional(),
  location: z.string().trim().max(150, "tooLong").optional(),
  coverLetter: z.string().trim().max(MAX_COVER_LETTER, "tooLong").optional(),
  consent: z.boolean().refine((value) => value, "consentRequired"),
});

export type ApplyFormValues = z.infer<typeof applyFormSchema>;
