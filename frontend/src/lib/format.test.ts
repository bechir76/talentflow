import { describe, expect, it } from "vitest";
import { daysSince, formatSalaryRange, initialsOf } from "./format";

describe("format", () => {
  it("formats salary ranges in euro", () => {
    expect(formatSalaryRange(75000, 95000)).toBe("€75,000 – €95,000");
    expect(formatSalaryRange(75000, null)).toBe("≥ €75,000");
    expect(formatSalaryRange(null, null)).toBeUndefined();
  });

  it("builds initials from up to two name parts", () => {
    expect(initialsOf("Lena Schmidt")).toBe("LS");
    expect(initialsOf("anna maria keller")).toBe("AM");
    expect(initialsOf("  ")).toBe("?");
  });

  it("counts whole days since a timestamp", () => {
    const now = Date.parse("2026-10-14T12:00:00Z");
    expect(daysSince("2026-10-14T08:00:00Z", now)).toBe(0);
    expect(daysSince("2026-10-11T11:00:00Z", now)).toBe(3);
    expect(daysSince("2026-10-15T08:00:00Z", now)).toBe(0);
  });
});
