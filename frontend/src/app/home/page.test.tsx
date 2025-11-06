//import { render, screen } from "@testing-library/react";
// import { describe, test, it, expect } from "vitest";
// import HomePage from "./page";


// describe("<HomePage />", () => {
//     test("renders home page", () => {
//         render(<HomePage />);
//     })

// })

import { afterEach, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import HomePage from "./page";

// cleanup after each test
afterEach(() => {
  cleanup();
});

test("Home Page: Renders SmartBuoy heading", () => {
  render(<HomePage />);
  expect(
    screen.getByRole("heading", { level: 1, name: "Welcome to SmartBuoy" }),
  ).toBeDefined();
});

test("Home Page: 'Our Vision' link points to /vision", () => {
  render(<HomePage />);
  const visionLink = screen.getByRole("link", { name: /our vision/i });
  expect(visionLink.getAttribute("href")).toBe("/vision");
});

test("Home Page: 'Contact Us' link points to /contact", () => {
  render(<HomePage />);
  const contactLink = screen.getByRole("link", { name: /contact us/i });
  expect(contactLink.getAttribute("href")).toBe("/contact");
});

test("Home Page: GitHub link points to repository and opens in new tab", () => {
  render(<HomePage />);
  const githubLinks = screen.getAllByRole("link", { name: /view on github/i });
  const githubLink = githubLinks[0];
  expect(githubLink.getAttribute("href")).toContain("github.com");
  expect(githubLink.getAttribute("target")).toBe("_blank");
  expect(githubLink.getAttribute("rel")).toBe("noopener noreferrer");
});