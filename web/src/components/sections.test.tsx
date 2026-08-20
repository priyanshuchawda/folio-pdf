import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { FeatureGrid } from "@/components/FeatureGrid";
import { InstallSteps } from "@/components/InstallSteps";
import { SiteFooter } from "@/components/SiteFooter";
import { Hero } from "@/components/Hero";

describe("FeatureGrid", () => {
  it("shows three product pillars", () => {
    render(<FeatureGrid />);
    expect(screen.getByText("Built for thin RAM")).toBeInTheDocument();
    expect(screen.getByText("Long docs, fast jumps")).toBeInTheDocument();
    expect(screen.getByText("Telegram & Drive ready")).toBeInTheDocument();
  });
});

describe("InstallSteps", () => {
  it("includes APK link and adb snippet", () => {
    render(
      <InstallSteps
        downloadUrl="https://example.com/Folio-1.7.0.apk"
        apkName="Folio-1.7.0.apk"
      />,
    );
    expect(screen.getByTestId("install-apk-link")).toHaveAttribute(
      "href",
      "https://example.com/Folio-1.7.0.apk",
    );
    expect(screen.getByTestId("adb-snippet")).toHaveTextContent("adb install -r Folio-1.7.0.apk");
  });
});

describe("SiteFooter", () => {
  it("links release notes and GitHub", () => {
    render(<SiteFooter releaseUrl="https://github.com/priyanshuchawda/folio-pdf/releases/tag/v1.7.0" />);
    expect(screen.getByTestId("footer-github")).toHaveAttribute(
      "href",
      "https://github.com/priyanshuchawda/folio-pdf",
    );
    expect(screen.getByTestId("footer-release").getAttribute("href")).toContain("releases");
  });
});

describe("Hero", () => {
  it("renders Folio brand headline", () => {
    render(<Hero downloadUrl="https://example.com/a.apk" apkLabel="a.apk" tag="v1" />);
    expect(screen.getByRole("heading", { level: 1, name: "Folio" })).toBeInTheDocument();
    expect(screen.getByTestId("cta-download")).toBeInTheDocument();
  });
});
