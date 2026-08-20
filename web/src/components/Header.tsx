import Link from "next/link";
import { SITE } from "@/lib/github";

type HeaderProps = {
  downloadUrl: string;
};

export function Header({ downloadUrl }: HeaderProps) {
  return (
    <header className="relative z-10 mx-auto flex w-full max-w-5xl items-center justify-between px-6 pb-2 pt-7 sm:px-8 sm:pt-9">
      <Link
        href="/"
        className="font-display text-[1.35rem] font-bold tracking-tight text-[var(--paper)]"
        data-testid="brand-link"
      >
        Folio
      </Link>
      <nav className="flex items-center gap-5 text-sm text-[var(--mist)]" aria-label="Primary">
        <a
          href={SITE.repoUrl}
          className="transition-opacity hover:opacity-80"
          target="_blank"
          rel="noreferrer"
          data-testid="nav-github"
        >
          GitHub
        </a>
        <a
          href={downloadUrl}
          className="rounded-full border border-[var(--line)] bg-[var(--ink-soft)] px-4 py-2 font-medium text-[var(--paper)] transition hover:border-[var(--leaf)]/40"
          data-testid="nav-download"
        >
          Download
        </a>
      </nav>
    </header>
  );
}
