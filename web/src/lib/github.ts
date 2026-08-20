export const SITE = {
  name: "Folio",
  tagline: "Lite PDF for tablets",
  repo: "priyanshuchawda/folio-pdf",
  repoUrl: "https://github.com/priyanshuchawda/folio-pdf",
  latestReleaseUrl: "https://github.com/priyanshuchawda/folio-pdf/releases/latest",
  packageId: "com.pulse.pdf",
} as const;

export type ReleaseAsset = {
  name: string;
  size: number;
  browser_download_url: string;
};

export type LatestRelease = {
  tag: string;
  name: string;
  publishedAt: string | null;
  apk: ReleaseAsset | null;
  htmlUrl: string;
};

function pickApk(assets: Array<{ name: string; size: number; browser_download_url: string }>) {
  const named = assets.find((a) => /^Folio-.*\.apk$/i.test(a.name));
  const anyApk = assets.find((a) => a.name.toLowerCase().endsWith(".apk"));
  return named ?? anyApk ?? null;
}

/** Fetches the latest GitHub release (cached ~1 hour). */
export async function getLatestRelease(): Promise<LatestRelease> {
  const fallback: LatestRelease = {
    tag: "v1.7.0",
    name: "Folio 1.7.0",
    publishedAt: null,
    htmlUrl: SITE.latestReleaseUrl,
    apk: {
      name: "Folio-1.7.0.apk",
      size: 4_680_355,
      browser_download_url:
        "https://github.com/priyanshuchawda/folio-pdf/releases/download/v1.7.0/Folio-1.7.0.apk",
    },
  };

  try {
    const res = await fetch(
      `https://api.github.com/repos/${SITE.repo}/releases/latest`,
      {
        headers: {
          Accept: "application/vnd.github+json",
          "User-Agent": "folio-website",
        },
        next: { revalidate: 3600 },
      },
    );
    if (!res.ok) return fallback;
    const data = (await res.json()) as {
      tag_name: string;
      name: string | null;
      published_at: string | null;
      html_url: string;
      assets: Array<{ name: string; size: number; browser_download_url: string }>;
    };
    const apk = pickApk(data.assets ?? []);
    return {
      tag: data.tag_name,
      name: data.name || data.tag_name,
      publishedAt: data.published_at,
      htmlUrl: data.html_url,
      apk: apk
        ? {
            name: apk.name,
            size: apk.size,
            browser_download_url: apk.browser_download_url,
          }
        : fallback.apk,
    };
  } catch {
    return fallback;
  }
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
