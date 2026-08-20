#!/usr/bin/env bash
# Build Folio release APK and publish a GitHub Release with downloadable asset.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

AUTHOR_NAME="${AUTHOR_NAME:-priyanshuchawda}"
AUTHOR_EMAIL="${AUTHOR_EMAIL:-priyanshuchawda@users.noreply.github.com}"
REPO="${REPO:-priyanshuchawda/folio-pdf}"

export GIT_AUTHOR_NAME="$AUTHOR_NAME"
export GIT_AUTHOR_EMAIL="$AUTHOR_EMAIL"
export GIT_COMMITTER_NAME="$AUTHOR_NAME"
export GIT_COMMITTER_EMAIL="$AUTHOR_EMAIL"

# Read version from gradle
VERSION_NAME="$(rg -oN 'versionName\s*=\s*"([^"]+)"' app/build.gradle.kts | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
VERSION_CODE="$(rg -oN 'versionCode\s*=\s*([0-9]+)' app/build.gradle.kts | head -1 | sed -E 's/.*=\s*([0-9]+)/\1/')"
TAG="v${VERSION_NAME}"
ASSET_NAME="Folio-${VERSION_NAME}.apk"

echo "== Folio release ${TAG} (versionCode ${VERSION_CODE}) =="

./gradlew :app:assembleRelease

APK_SRC="app/build/outputs/apk/release/app-release.apk"
test -f "$APK_SRC"
mkdir -p dist
cp -f "$APK_SRC" "dist/${ASSET_NAME}"
ls -lh "dist/${ASSET_NAME}"

# Ensure latest docs are on remote before tagging
bash ./scripts/push-github.sh

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag ${TAG} already exists locally."
else
  git tag -a "$TAG" -m "Folio ${VERSION_NAME}"
fi
git push origin "$TAG" || true

NOTES="$(cat <<EOF
## Folio ${VERSION_NAME}

Ultra-light PDF reader for low-RAM Android tablets (Fire HD 8 / Lineage-class).

### Download
- **${ASSET_NAME}** — installable release APK (arm64-v8a)

### Highlights
- Pdfium vertical reader — fast on 1000+ page PDFs
- Go-to-page (tap page label) + Drive-style right scrubber
- Auto-hiding chrome, battery-friendly wake/brightness
- Opens from Telegram, Drive, Files (VIEW + Share)

### Install
1. Download the APK below
2. Allow install from unknown sources if asked
3. Open a PDF → choose **Folio**

\`\`\`bash
adb install -r ${ASSET_NAME}
\`\`\`

**versionCode:** ${VERSION_CODE}  
**Package:** \`com.pulse.pdf\`
EOF
)"

if gh release view "$TAG" -R "$REPO" >/dev/null 2>&1; then
  echo "Release ${TAG} exists — uploading asset (clobber)..."
  gh release upload "$TAG" "dist/${ASSET_NAME}" -R "$REPO" --clobber
  gh release edit "$TAG" -R "$REPO" --title "Folio ${VERSION_NAME}" --notes "$NOTES"
else
  gh release create "$TAG" "dist/${ASSET_NAME}" \
    -R "$REPO" \
    --title "Folio ${VERSION_NAME}" \
    --notes "$NOTES" \
    --latest
fi

echo "Release URL: https://github.com/${REPO}/releases/tag/${TAG}"
echo "Latest:      https://github.com/${REPO}/releases/latest"
