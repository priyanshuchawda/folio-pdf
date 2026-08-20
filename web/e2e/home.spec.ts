import { expect, test } from "@playwright/test";

test.describe("Folio landing", () => {
  test("loads brand, download buttons, and GitHub links", async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { level: 1, name: "Folio" })).toBeVisible();

    const cta = page.getByTestId("cta-download");
    await expect(cta).toBeVisible();
    await expect(cta).toHaveAttribute("href", /releases\/download\/.*\.apk/);

    await expect(page.getByTestId("cta-github")).toHaveAttribute(
      "href",
      "https://github.com/priyanshuchawda/folio-pdf",
    );
    await expect(page.getByTestId("nav-github")).toBeVisible();
    await expect(page.getByTestId("features")).toBeVisible();
    await expect(page.getByTestId("install")).toBeVisible();
    await expect(page.getByTestId("footer-github")).toBeVisible();
  });

  test("download CTAs are tappable on current viewport", async ({ page }) => {
    await page.goto("/");
    const cta = page.getByTestId("cta-download");
    const box = await cta.boundingBox();
    expect(box).toBeTruthy();
    expect(box!.height).toBeGreaterThanOrEqual(40);
    expect(box!.width).toBeGreaterThanOrEqual(120);

    const nav = page.getByTestId("nav-download");
    await expect(nav).toBeVisible();
    await nav.click({ trial: true });
  });

  test("mobile layout keeps hero readable without horizontal overflow", async ({ page }, testInfo) => {
    test.skip(!testInfo.project.name.startsWith("mobile"), "mobile projects only");
    await page.goto("/");
    const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
    const clientWidth = await page.evaluate(() => document.documentElement.clientWidth);
    expect(scrollWidth).toBeLessThanOrEqual(clientWidth + 1);

    await expect(page.getByTestId("hero")).toBeVisible();
    await expect(page.getByTestId("device-preview")).toBeVisible();
  });
});
