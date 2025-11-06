//import { render, screen } from "@testing-library/react";
// import { describe, test, it, expect } from "vitest";
// import HomePage from "./page";


// describe("<HomePage />", () => {
//     test("renders home page", () => {
//         render(<HomePage />);
//     })

// })

import { expect, test } from "vitest";
import { render, screen } from "@testing-library/react";
import HomePage from "./page";

test("Home Page: Renders SmartBuoy heading", () => {
  render(<HomePage />);
  expect(
    screen.getByRole("heading", { level: 1, name: "Welcome to SmartBuoy" }),
  ).toBeDefined();
});


