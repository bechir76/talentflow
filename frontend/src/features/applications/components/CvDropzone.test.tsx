import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import CvDropzone from "./CvDropzone";

describe("CvDropzone", () => {
  it("accepts a dropped file", () => {
    const onChange = vi.fn();
    render(<CvDropzone file={undefined} onChange={onChange}/>);
    const file = new File(["%PDF-1.7"], "lena.pdf", { type: "application/pdf" });

    fireEvent.drop(screen.getByRole("button", { name: /drop your cv here/i }), { dataTransfer: { files: [file] } });

    expect(onChange).toHaveBeenCalledWith(file);
  });

  it("shows the selected file and lets the candidate replace it", async () => {
    const onChange = vi.fn();
    render(<CvDropzone file={new File(["x".repeat(2048)], "lena.pdf")} onChange={onChange}/>);

    expect(screen.getByText("lena.pdf")).toBeInTheDocument();
    expect(screen.getByText("2 KB")).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: "Replace" }));

    expect(onChange).toHaveBeenCalledWith(undefined);
  });
});
