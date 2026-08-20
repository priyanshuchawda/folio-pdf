import "@testing-library/jest-dom/vitest";
import React from "react";
import { vi } from "vitest";

vi.mock("next/link", () => {
  function MockLink(props: Record<string, unknown>) {
    const { children, href, ...rest } = props;
    return React.createElement("a", { href, ...rest }, children as React.ReactNode);
  }
  return { default: MockLink };
});
