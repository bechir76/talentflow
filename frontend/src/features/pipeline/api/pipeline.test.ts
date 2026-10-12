import { describe, expect, it } from "vitest";
import { canMove } from "./pipeline";

describe("canMove (mirrors the backend stage rules)", () => {
  it("allows moves between active stages in both directions", () => {
    expect(canMove("APPLIED", "INTERVIEW")).toBe(true);
    expect(canMove("OFFER", "SCREENING")).toBe(true);
  });

  it("only hires candidates with an offer", () => {
    expect(canMove("INTERVIEW", "HIRED")).toBe(false);
    expect(canMove("OFFER", "HIRED")).toBe(true);
  });

  it("rejects from any active stage and keeps finished applications in place", () => {
    expect(canMove("APPLIED", "REJECTED")).toBe(true);
    expect(canMove("REJECTED", "APPLIED")).toBe(false);
    expect(canMove("HIRED", "OFFER")).toBe(false);
    expect(canMove("SCREENING", "SCREENING")).toBe(false);
  });
});
