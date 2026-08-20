import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { Header } from "@/components/Header";

describe("Header", () => {
  it("links brand home and exposes GitHub + Download actions", () => {
    render(<Header downloadUrl="https://example.com/Folio.apk" />);

    expect(screen.getByTestId("brand-link")).toHaveAttribute("href", "/");
    expect(screen.getByTestId("nav-github")).toHaveAttribute(
      "href",
      "https://github.com/priyanshuchawda/folio-pdf",
    );
    expect(screen.getByTestId("nav-download")).toHaveAttribute(
      "href",
      "https://example.com/Folio.apk",
    );
  });
});
