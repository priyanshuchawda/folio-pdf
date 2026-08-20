# Folio website

Marketing site for [Folio](https://github.com/priyanshuchawda/folio-pdf) — lite PDF reader for Android tablets.

## Develop

```bash
cd web
npm install
npm run dev
```

## Deploy (Vercel)

```bash
cd web
npx vercel --prod
```

Root directory on Vercel: `web` (if linked from the folio-pdf monorepo).

Download buttons resolve the latest GitHub Release APK via the GitHub API (revalidated hourly).
