# Folio

Ultra-light PDF reader for low-RAM Android tablets (Fire HD 8 / Lineage-class devices).

## Why Folio feels like Drive

Google Drive’s viewer uses **PDFium** (native). Folio now uses the same engine family
via android-pdf-viewer — not Android’s slow `PdfRenderer` — so 1000+ page docs open
instantly and pages paint while you scroll.

- **Vertical scroll**: pages stack top→bottom
- **Lazy decode**: only nearby pages render (Pdfium)
- **Battery**: screen wake ~45s after touch; dimmer default brightness
- **Not forced as default**: pick Folio in Open with when you want it

## Build

```bash
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

## Publish

```bash
./scripts/push-github.sh
```

## License

MIT
