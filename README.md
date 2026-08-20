# Folio

Ultra-light PDF reader for low-RAM Android tablets (Fire HD 8 / Lineage-class devices).

## Why Folio

- **Vertical scroll**: pages stack top→bottom (swipe down to read), not left/right
- **Low RAM**: screen-width render, RGB_565 page cache, neighbor prefetch only
- **Battery**: keeps screen on only while you interact (~45s idle release), dimmer default brightness
- **Simple**: open PDFs from the library or “Open with” — not forced as the system default

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
