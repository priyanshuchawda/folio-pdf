import { SITE } from "@/lib/github";

type SiteFooterProps = {
  releaseUrl: string;
};

export function SiteFooter({ releaseUrl }: SiteFooterProps) {
  return (
    <footer className="relative z-10 border-t border-[var(--line)]" data-testid="footer">
      <div className="mx-auto flex max-w-5xl flex-col gap-3 px-6 py-8 text-sm text-[var(--muted)] sm:flex-row sm:items-center sm:justify-between sm:px-8">
        <p>
          <span className="font-display font-semibold text-[var(--paper)]">Folio</span>
          {" — "}
          {SITE.tagline}
        </p>
        <div className="flex gap-5">
          <a
            href={releaseUrl}
            target="_blank"
            rel="noreferrer"
            className="hover:text-[var(--paper)]"
            data-testid="footer-release"
          >
            Release notes
          </a>
          <a
            href={SITE.repoUrl}
            target="_blank"
            rel="noreferrer"
            className="hover:text-[var(--paper)]"
            data-testid="footer-github"
          >
            GitHub
          </a>
        </div>
      </div>
    </footer>
  );
}
