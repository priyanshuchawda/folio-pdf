# Folio website

Marketing site for [Folio](https://github.com/priyanshuchawda/folio-pdf) — lite PDF reader for Android tablets.

**Live:** https://folio-pdf-seven.vercel.app  
**Repo:** https://github.com/priyanshuchawda/folio-pdf (`web/` directory)

## Develop

```bash
cd web
npm install
npm run dev
```

## Tests

```bash
npm run test          # Vitest unit + component tests
npm run test:e2e      # Playwright (desktop + Pixel 7 mobile)
npm run test:all      # unit + e2e
```

Coverage includes download/GitHub buttons, header/footer links, feature sections, and mobile layout (no horizontal overflow, tap-target size).

## Deploy (Vercel)

Linked to GitHub `priyanshuchawda/folio-pdf` with **Root Directory = `web`**.

```bash
cd web
npx vercel --prod
```

Pushing to `main` triggers production deploys. Download buttons resolve the latest GitHub Release APK via the GitHub API (revalidated hourly).
