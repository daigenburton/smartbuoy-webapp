import { expect, test } from "vitest";
import { render, screen } from "@testing-library/react";
import Contact from "./page";

test("Contact Page: Renders contact heading and names", () => {
  render(<Contact />);
  expect(
    screen.getByRole("heading", { level: 1, name: "Contact Us" }),
  ).toBeDefined();

  // Check that one known team member’s name appears
  expect(screen.getByText(/Nandana Alwarappan/i)).toBeDefined();
});
