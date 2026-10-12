import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import SkillsInput from "./SkillsInput";

function Harness({ initial = [] as string[] }) {
  const [skills, setSkills] = useState(initial);
  return <>
    <SkillsInput value={skills} onChange={setSkills} onBlur={() => undefined}/>
    <output>{skills.join("|")}</output>
  </>;
}

describe("SkillsInput", () => {
  it("adds skills with Enter or comma and ignores duplicates", async () => {
    render(<Harness/>);
    const input = screen.getByPlaceholderText("Type a skill and press Enter");

    await userEvent.type(input, "Java{Enter}Spring Boot,java{Enter}");

    expect(screen.getByRole("status")).toHaveTextContent("Java|Spring Boot");
  });

  it("removes a skill with its button or Backspace", async () => {
    render(<Harness initial={["Java", "Docker", "SQL"]}/>);

    await userEvent.click(screen.getByRole("button", { name: "Remove Docker" }));
    await userEvent.type(screen.getByPlaceholderText("Type a skill and press Enter"), "{Backspace}");

    expect(screen.getByRole("status")).toHaveTextContent("Java");
  });
});
