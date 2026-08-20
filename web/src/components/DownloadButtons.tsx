type DownloadButtonsProps = {
  downloadUrl: string;
  apkLabel: string;
  tag?: string;
};

export function DownloadButtons({ downloadUrl, apkLabel, tag }: DownloadButtonsProps) {
  return (
    <div data-testid="download-block">
      <div className="anim-rise-delay mt-9 flex flex-wrap items-center gap-3">
        <a
          href={downloadUrl}
          className="inline-flex min-h-11 min-w-[8.5rem] items-center justify-center rounded-full bg-[var(--leaf)] px-7 py-3.5 font-display text-[0.95rem] font-bold tracking-wide text-[var(--ink)] transition hover:brightness-110"
          data-testid="cta-download"
        >
          Download APK
        </a>
        <a
          href="https://github.com/priyanshuchawda/folio-pdf"
          target="_blank"
          rel="noreferrer"
          className="inline-flex min-h-11 items-center justify-center rounded-full border border-[var(--line)] px-6 py-3.5 text-[0.95rem] text-[var(--mist)] transition hover:border-[var(--mist)]/35 hover:text-[var(--paper)]"
          data-testid="cta-github"
        >
          Source on GitHub
        </a>
      </div>
      <p className="anim-rise-delay-2 mt-4 text-sm text-[var(--muted)]" data-testid="apk-meta">
        {apkLabel}
        {tag ? ` · ${tag}` : ""}
      </p>
    </div>
  );
}
