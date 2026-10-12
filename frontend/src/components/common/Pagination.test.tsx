import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import Pagination from "./Pagination";

describe("Pagination", () => {
  it("shows the visible range and disables Previous on the first page", () => {
    render(<Pagination page={0} size={10} totalElements={42} totalPages={5} onPageChange={() => undefined}/>);

    expect(screen.getByText("Showing 1–10 of 42")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Previous" })).toBeDisabled();
    expect(screen.getByRole("button", { name: "1" })).toHaveAttribute("aria-current", "page");
  });

  it("reports the chosen page", async () => {
    const onPageChange = vi.fn();
    render(<Pagination page={2} size={10} totalElements={42} totalPages={5} onPageChange={onPageChange}/>);

    await userEvent.click(screen.getByRole("button", { name: "Next" }));
    await userEvent.click(screen.getByRole("button", { name: "5" }));

    expect(onPageChange).toHaveBeenNthCalledWith(1, 3);
    expect(onPageChange).toHaveBeenNthCalledWith(2, 4);
    expect(screen.getByText("Showing 21–30 of 42")).toBeInTheDocument();
  });
});
