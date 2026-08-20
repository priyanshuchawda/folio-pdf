import { DownloadButtons } from "@/components/DownloadButtons";

type HeroProps = {
  downloadUrl: string;
  apkLabel: string;
  tag?: string;
};

export function Hero({ downloadUrl, apkLabel, tag }: HeroProps) {
  return (
    <div className="anim-rise max-w-xl" data-testid="hero">
      <p className="font-display mb-5 text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-[var(--leaf)]">
        Android · arm64
      </p>
      <h1 className="font-display text-[clamp(3.4rem,11vw,6.4rem)] font-extrabold leading-[0.92] tracking-[-0.04em] text-[var(--paper)]">
        Folio
      </h1>
      <p className="mt-5 max-w-md text-[1.15rem] leading-relaxed text-[var(--mist)] sm:text-[1.25rem]">
        Ultra-light PDF reading for low-RAM tablets — Telegram, Drive, and 1000+ page
        textbooks without the bloat.
      </p>
      <DownloadButtons downloadUrl={downloadUrl} apkLabel={apkLabel} tag={tag} />
    </div>
  );
}
