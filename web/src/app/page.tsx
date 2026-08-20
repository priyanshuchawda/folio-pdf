import { Header } from "@/components/Header";
import { Hero } from "@/components/Hero";
import { DevicePreview } from "@/components/DevicePreview";
import { FeatureGrid } from "@/components/FeatureGrid";
import { InstallSteps } from "@/components/InstallSteps";
import { SiteFooter } from "@/components/SiteFooter";
import { getLatestRelease, formatBytes, SITE } from "@/lib/github";

export const dynamic = "force-static";
export const revalidate = 3600;

export default async function HomePage() {
  const release = await getLatestRelease();
  const downloadUrl = release.apk?.browser_download_url ?? SITE.latestReleaseUrl;
  const apkLabel = release.apk
    ? `${release.apk.name} · ${formatBytes(release.apk.size)}`
    : "Download APK";
  const apkName = release.apk?.name ?? "Folio.apk";

  return (
    <div className="relative min-h-screen overflow-x-hidden">
      <div
        aria-hidden
        className="pointer-events-none absolute inset-0 opacity-[0.07]"
        style={{
          backgroundImage:
            "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E\")",
        }}
      />

      <Header downloadUrl={downloadUrl} />

      <main className="relative z-10">
        <section className="relative mx-auto grid min-h-[calc(100svh-5.5rem)] w-full max-w-5xl grid-cols-1 items-center gap-10 px-6 pb-16 pt-10 sm:px-8 lg:grid-cols-[1.05fr_0.95fr] lg:gap-8 lg:pb-20 lg:pt-6">
          <Hero downloadUrl={downloadUrl} apkLabel={apkLabel} tag={release.tag} />
          <DevicePreview />
        </section>

        <FeatureGrid />
        <InstallSteps downloadUrl={downloadUrl} apkName={apkName} />
      </main>

      <SiteFooter releaseUrl={release.htmlUrl} />
    </div>
  );
}
