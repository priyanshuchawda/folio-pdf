import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { DownloadButtons } from "@/components/DownloadButtons";

describe("DownloadButtons", () => {
  const apkUrl =
    "https://github.com/priyanshuchawda/folio-pdf/releases/download/v1.7.0/Folio-1.7.0.apk";

  it("renders primary download and GitHub CTAs", () => {
    render(
      <DownloadButtons downloadUrl={apkUrl} apkLabel="Folio-1.7.0.apk · 4.5 MB" tag="v1.7.0" />,
    );

    const download = screen.getByTestId("cta-download");
    const github = screen.getByTestId("cta-github");

    expect(download).toHaveAttribute("href", apkUrl);
    expect(download).toHaveTextContent("Download APK");
    expect(github).toHaveAttribute("href", "https://github.com/priyanshuchawda/folio-pdf");
    expect(screen.getByTestId("apk-meta")).toHaveTextContent("Folio-1.7.0.apk · 4.5 MB · v1.7.0");
  });

  it("keeps tap targets reasonably large for mobile", () => {
    render(<DownloadButtons downloadUrl={apkUrl} apkLabel="APK" />);
    const download = screen.getByTestId("cta-download");
    expect(download.className).toMatch(/min-h-11/);
  });
});
