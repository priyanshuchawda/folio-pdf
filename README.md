# Folio

**Ultra-light PDF reader for Android tablets** — built for low-RAM devices like Amazon Fire HD 8 (LineageOS / custom ROMs) where Google Drive’s viewer is heavy or unreliable.

[![Release](https://img.shields.io/github/v/release/priyanshuchawda/folio-pdf?style=flat-square)](https://github.com/priyanshuchawda/folio-pdf/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-8.0%2B-green.svg?style=flat-square)](https://github.com/priyanshuchawda/folio-pdf/releases)

**Download latest APK:** [Releases →](https://github.com/priyanshuchawda/folio-pdf/releases/latest)  
**Website:** [folio-pdf-seven.vercel.app](https://folio-pdf-seven.vercel.app)

---

## Why Folio

| Problem | Folio |
| --- | --- |
| Drive / Chrome PDF viewer struggles on 1–2 GB tablets | Pdfium engine, small page cache, RGB_565 bitmaps |
| 1000+ page textbooks take forever to open | Lazy decode — open in ~1 s, paint as you scroll |
| Need Drive-like scrubbing on long docs | Right-edge page scrubber + tap-to-type go-to-page |
| Battery drain while reading | Dim default brightness, screen-on only ~30 s after touch, chrome auto-hides |

Package ID: `com.pulse.pdf` · Min SDK 26 (Android 8) · arm64-v8a

---

## Features

- **Vertical continuous scroll** (top → bottom), like Drive
- **Pdfium** rendering (same native engine family as Chrome / Drive)
- **Go to page** — tap `N / Total · tap to go`, type a number, jump
- **Drive-style scrubber** on the right edge for long PDFs
- **Auto-hiding header & footer** — tap page to show; hides on idle / scroll
- **Recent files** with last-page resume
- **Open from Telegram, Drive, Files, Downloads** (VIEW + Share/SEND)
- Handles Telegram `application/octet-stream` PDFs
- Fallback cache copy when a content provider won’t give a seekable FD
- **Not forced as system default** — choose Folio in “Open with” when you want it

---

## Install (tablet)

1. Open the [latest release](https://github.com/priyanshuchawda/folio-pdf/releases/latest)
2. Download **`Folio-x.y.z.apk`**
3. On the tablet: allow install from your browser/file manager if prompted
4. Install → open a PDF → pick **Folio**

### From Telegram

1. Download the PDF in Telegram  
2. Tap the file → **Open with** → **Folio**  
3. Use “Just once” unless you want Folio every time  

### From ADB

```bash
adb install -r Folio-1.7.0.apk
```

---

## Usage tips

- **Tap the page** — show / hide title + page bar  
- **Tap the page label** — type a page number and go  
- **Drag the right scrubber** — jump through 1000+ page docs  
- **Back** — if chrome is hidden, first back shows it; again exits  

---

## Build from source

Requirements: JDK 17+, Android SDK, device or emulator (arm64 preferred).

```bash
git clone https://github.com/priyanshuchawda/folio-pdf.git
cd folio-pdf
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Optional device smoke test:

```bash
./scripts/test-on-device.sh
```

Publish / release helpers:

```bash
./scripts/push-github.sh    # commit + push to GitHub
./scripts/release.sh        # build APK + create GitHub Release
```

---

## Battery & memory profile

Tuned for **~1.4 GB Fire HD–class** tablets:

- Pdfium page-part cache reduced (default library cache is far too large)
- `RGB_565` page bitmaps (not ARGB_8888)
- Annotation rendering off
- Screen wake lock released after **30 s** idle
- Default brightness ~40%
- `largeHeap=false` — stay a good neighbour to other apps

On-device check with a ~1800-page / ~200 MB PDF typically stays around **~30 MB** process PSS while reading.

---

## Architecture (short)

```
Telegram / Drive / Files
        │  content:// or file:// (+ grant)
        ▼
  ReaderActivity  ──►  PDFView (android-pdf-viewer / Pdfium)
        │                 vertical scroll, lazy tiles
        ├── FolioScrollHandle (scrubber)
        ├── go-to-page dialog
        └── ScreenWakeGuard + chrome auto-hide
```

Legacy `PdfRenderer` experiments remain in-tree unused; the reader path is Pdfium-only.

---

## Privacy

- No accounts, ads, or analytics  
- PDFs are read from the URI you open; optional cache copies stay in app cache and are trimmed  
- No network permission required for reading  

---

## Roadmap / ideas

- Optional night mode toggle  
- Bookmarks / table of contents if the PDF has one  
- More ABIs if needed (currently **arm64-v8a** only for size)

PRs welcome.

---

## License

[MIT](LICENSE) © priyanshuchawda

Pdfium via [mhiew/AndroidPdfViewer](https://github.com/mhiew/AndroidPdfViewer) (Apache-2.0 / related notices apply to that dependency).
