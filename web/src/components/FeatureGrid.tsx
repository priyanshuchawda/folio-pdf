const FEATURES = [
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
] as const;

export function FeatureGrid() {
  return (
    <section className="border-t border-[var(--line)] bg-[rgba(11,16,14,0.55)]" data-testid="features">
      <div className="mx-auto grid max-w-5xl gap-10 px-6 py-16 sm:px-8 md:grid-cols-3 md:gap-8">
        {FEATURES.map((item) => (
          <div key={item.title}>
            <h2 className="font-display text-xl font-bold tracking-tight text-[var(--paper)]">
              {item.title}
            </h2>
            <p className="mt-3 text-[0.98rem] leading-relaxed text-[var(--muted)]">{item.body}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
