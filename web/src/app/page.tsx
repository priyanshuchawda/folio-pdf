import Link from "next/link";
import { getLatestRelease, formatBytes, SITE } from "@/lib/github";

export const dynamic = "force-static";
export const revalidate = 3600;

export default async function HomePage() {
  const release = await getLatestRelease();
  const downloadUrl = release.apk?.browser_download_url ?? SITE.latestReleaseUrl;
  const apkLabel = release.apk
    ? `${release.apk.name} · ${formatBytes(release.apk.size)}`
    : "Download APK";

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

      <header className="relative z-10 mx-auto flex w-full max-w-5xl items-center justify-between px-6 pb-2 pt-7 sm:px-8 sm:pt-9">
        <Link href="/" className="font-display text-[1.35rem] font-bold tracking-tight text-[var(--paper)]">
          Folio
        </Link>
        <nav className="flex items-center gap-5 text-sm text-[var(--mist)]">
          <a
            href={SITE.repoUrl}
            className="transition-opacity hover:opacity-80"
            target="_blank"
            rel="noreferrer"
          >
            GitHub
          </a>
          <a
            href={downloadUrl}
            className="rounded-full border border-[var(--line)] bg-[var(--ink-soft)] px-4 py-2 font-medium text-[var(--paper)] transition hover:border-[var(--leaf)]/40"
          >
            Download
          </a>
        </nav>
      </header>

      <main className="relative z-10">
        {/* Hero — one composition */}
        <section className="relative mx-auto grid min-h-[calc(100svh-5.5rem)] w-full max-w-5xl grid-cols-1 items-center gap-10 px-6 pb-16 pt-10 sm:px-8 lg:grid-cols-[1.05fr_0.95fr] lg:gap-8 lg:pb-20 lg:pt-6">
          <div className="anim-rise max-w-xl">
            <p className="font-display mb-5 text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-[var(--leaf)]">
              Android · arm64
            </p>
            <h1 className="font-display text-[clamp(3.4rem,11vw,6.4rem)] font-extrabold leading-[0.92] tracking-[-0.04em] text-[var(--paper)]">
              Folio
            </h1>
            <p className="mt-5 max-w-md text-[1.15rem] leading-relaxed text-[var(--mist)] sm:text-[1.25rem]">
              Ultra-light PDF reading for low-RAM tablets — Telegram, Drive, and
              1000+ page textbooks without the bloat.
            </p>
            <div className="anim-rise-delay mt-9 flex flex-wrap items-center gap-3">
              <a
                href={downloadUrl}
                className="inline-flex items-center justify-center rounded-full bg-[var(--leaf)] px-7 py-3.5 font-display text-[0.95rem] font-bold tracking-wide text-[var(--ink)] transition hover:brightness-110"
              >
                Download APK
              </a>
              <a
                href={SITE.repoUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center justify-center rounded-full border border-[var(--line)] px-6 py-3.5 text-[0.95rem] text-[var(--mist)] transition hover:border-[var(--mist)]/35 hover:text-[var(--paper)]"
              >
                Source on GitHub
              </a>
            </div>
            <p className="anim-rise-delay-2 mt-4 text-sm text-[var(--muted)]">
              {apkLabel}
              {release.tag ? ` · ${release.tag}` : ""}
            </p>
          </div>

          <div className="anim-rise-delay relative mx-auto w-full max-w-[340px] lg:max-w-none">
            <div
              aria-hidden
              className="anim-drift absolute -inset-8 rounded-[2rem] opacity-70 blur-2xl"
              style={{
                background:
                  "radial-gradient(circle at 40% 30%, var(--glow), transparent 60%)",
              }}
            />
            <div className="relative overflow-hidden rounded-[1.6rem] border border-[var(--line)] bg-[linear-gradient(160deg,#141c17_0%,#0e1411_100%)] shadow-[0_30px_80px_rgba(0,0,0,0.45)]">
              <div className="flex items-center justify-between border-b border-[var(--line)] px-4 py-3">
                <span className="text-xs text-[var(--muted)]">cn_networks.pdf</span>
                <span className="font-display text-xs font-semibold text-[var(--leaf)]">
                  142 / 1831
                </span>
              </div>
              <div className="relative aspect-[3/4] bg-[linear-gradient(180deg,#1a221c_0%,#121914_100%)] p-5">
                <div className="h-full rounded-md border border-[var(--line)] bg-[#f3f0e6] p-4 text-[#1c1a14] shadow-inner">
                  <div className="mb-3 h-2 w-1/3 rounded bg-[#cfc8b4]/60" />
                  <div className="space-y-2">
                    <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[92%] rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[86%] rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[78%] rounded bg-[#d6d0be]" />
                  </div>
                  <div className="mt-6 space-y-2 opacity-80">
                    <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[90%] rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[95%] rounded bg-[#d6d0be]" />
                    <div className="h-1.5 w-[70%] rounded bg-[#d6d0be]" />
                  </div>
                </div>
                <div className="absolute right-3 top-1/3 flex h-16 w-11 items-center justify-center rounded-l-xl bg-[#1b3a2f]/95 text-center font-display text-[0.65rem] font-bold leading-tight text-white shadow-lg">
                  142
                  <br />
                  <span className="font-normal opacity-70">/1831</span>
                </div>
              </div>
              <div className="border-t border-[var(--line)] px-4 py-3 text-center text-sm text-[var(--mist)]">
                142 / 1831 · tap to go
              </div>
            </div>
          </div>
        </section>

        <section className="border-t border-[var(--line)] bg-[rgba(11,16,14,0.55)]">
          <div className="mx-auto grid max-w-5xl gap-10 px-6 py-16 sm:px-8 md:grid-cols-3 md:gap-8">
            {[
              {
                title: "Built for thin RAM",
                body: "Pdfium tiles, small page cache, RGB_565 bitmaps — stays light on Fire HD–class tablets.",
              },
              {
                title: "Long docs, fast jumps",
                body: "Vertical scroll, right-edge scrubber, and tap-to-type go-to-page for 1000+ page books.",
              },
              {
                title: "Telegram & Drive ready",
                body: "Open or Share a PDF into Folio. Session grants and cache fallback when providers are picky.",
              },
            ].map((item) => (
              <div key={item.title}>
                <h2 className="font-display text-xl font-bold tracking-tight text-[var(--paper)]">
                  {item.title}
                </h2>
                <p className="mt-3 text-[0.98rem] leading-relaxed text-[var(--muted)]">
                  {item.body}
                </p>
              </div>
            ))}
          </div>
        </section>

        <section className="mx-auto max-w-5xl px-6 py-16 sm:px-8">
          <h2 className="font-display text-3xl font-bold tracking-tight text-[var(--paper)] sm:text-4xl">
            Install in a minute
          </h2>
          <ol className="mt-8 space-y-5 text-[var(--mist)]">
            <li className="flex gap-4">
              <span className="font-display text-[var(--leaf)]">01</span>
              <span>
                Download{" "}
                <a href={downloadUrl} className="underline decoration-[var(--leaf)]/50 underline-offset-4">
                  {release.apk?.name ?? "the latest APK"}
                </a>{" "}
                on your tablet.
              </span>
            </li>
            <li className="flex gap-4">
              <span className="font-display text-[var(--leaf)]">02</span>
              <span>Allow install from your browser or file manager if Android asks.</span>
            </li>
            <li className="flex gap-4">
              <span className="font-display text-[var(--leaf)]">03</span>
              <span>
                Open a PDF → choose <strong className="text-[var(--paper)]">Folio</strong>. From
                Telegram: download → Open with → Folio.
              </span>
            </li>
          </ol>

          <div className="mt-10 overflow-x-auto rounded-xl border border-[var(--line)] bg-[var(--ink-soft)] p-4">
            <code className="font-mono text-sm text-[var(--leaf)]">
              adb install -r {release.apk?.name ?? "Folio.apk"}
            </code>
          </div>

          <p className="mt-6 text-sm text-[var(--muted)]">
            Package <code className="text-[var(--mist)]">{SITE.packageId}</code> · Min Android 8 ·
            arm64-v8a · MIT license
          </p>
        </section>
      </main>

      <footer className="relative z-10 border-t border-[var(--line)]">
        <div className="mx-auto flex max-w-5xl flex-col gap-3 px-6 py-8 text-sm text-[var(--muted)] sm:flex-row sm:items-center sm:justify-between sm:px-8">
          <p>
            <span className="font-display font-semibold text-[var(--paper)]">Folio</span>
            {" — "}
            {SITE.tagline}
          </p>
          <div className="flex gap-5">
            <a href={release.htmlUrl} target="_blank" rel="noreferrer" className="hover:text-[var(--paper)]">
              Release notes
            </a>
            <a href={SITE.repoUrl} target="_blank" rel="noreferrer" className="hover:text-[var(--paper)]">
              GitHub
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
