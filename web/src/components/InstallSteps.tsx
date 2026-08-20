import { SITE } from "@/lib/github";

type InstallStepsProps = {
  downloadUrl: string;
  apkName: string;
};

export function InstallSteps({ downloadUrl, apkName }: InstallStepsProps) {
  return (
    <section className="mx-auto max-w-5xl px-6 py-16 sm:px-8" data-testid="install">
      <h2 className="font-display text-3xl font-bold tracking-tight text-[var(--paper)] sm:text-4xl">
        Install in a minute
      </h2>
      <ol className="mt-8 space-y-5 text-[var(--mist)]">
        <li className="flex gap-4">
          <span className="font-display text-[var(--leaf)]">01</span>
          <span>
            Download{" "}
            <a
              href={downloadUrl}
              className="underline decoration-[var(--leaf)]/50 underline-offset-4"
              data-testid="install-apk-link"
            >
              {apkName}
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
        <code className="font-mono text-sm text-[var(--leaf)]" data-testid="adb-snippet">
          adb install -r {apkName}
        </code>
      </div>

      <p className="mt-6 text-sm text-[var(--muted)]">
        Package <code className="text-[var(--mist)]">{SITE.packageId}</code> · Min Android 8 ·
        arm64-v8a · MIT license
      </p>
    </section>
  );
}
