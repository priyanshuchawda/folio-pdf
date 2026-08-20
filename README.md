# Folio

Ultra-light PDF reader for low-RAM Android tablets (Fire HD 8 / Lineage-class devices).

## Why Folio

- **Low RAM**: screen-fit rendering, RGB_565 page cache, one-page prefetch
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
