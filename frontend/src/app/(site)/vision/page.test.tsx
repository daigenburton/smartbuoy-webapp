import { expect, test } from "vitest";
import { render, screen } from "@testing-library/react";
import Vision from "./page";

test("Vision Page: Renders vision heading", () => {
  render(<Vision />);
  expect(
    screen.getByRole("heading", { level: 1, name: "Smarter Fishing, Safer Oceans" }),
  ).toBeDefined();
});
