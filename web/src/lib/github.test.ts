import { describe, expect, it } from "vitest";
import { formatBytes, SITE } from "@/lib/github";

describe("formatBytes", () => {
  it("formats kilobytes under 1 MB", () => {
    expect(formatBytes(512 * 1024)).toBe("512 KB");
  });

  it("formats megabytes with one decimal", () => {
    expect(formatBytes(4_680_355)).toBe("4.5 MB");
  });
});

describe("SITE constants", () => {
  it("points at the Folio GitHub repository", () => {
    expect(SITE.repo).toBe("priyanshuchawda/folio-pdf");
    expect(SITE.repoUrl).toContain("github.com/priyanshuchawda/folio-pdf");
    expect(SITE.packageId).toBe("com.pulse.pdf");
  });
});
