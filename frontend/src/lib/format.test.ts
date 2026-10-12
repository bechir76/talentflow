import { describe, expect, it } from "vitest";
import { formatSalaryRange, initialsOf } from "./format";

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
});
