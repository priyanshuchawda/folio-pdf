#!/usr/bin/env bash
# Build Folio, install on connected tablet, open sample PDF, verify UI via uiautomator.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ADB="${ADB:-adb}"
PKG=com.pulse.pdf
APK=app/build/outputs/apk/release/app-release.apk

echo "== build =="
./gradlew :app:assembleRelease

echo "== install =="
$ADB install -r "$APK"

echo "== trim noise apps =="
$ADB shell am force-stop com.android.vending || true
$ADB shell am force-stop com.google.android.youtube || true

echo "== launch library =="
$ADB shell am force-stop "$PKG"
$ADB shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null
sleep 2

echo "== tap Sample =="
$ADB shell uiautomator dump /sdcard/folio_ui.xml >/dev/null
# Prefer text match; fallback coordinate near top buttons on 800x1280
if $ADB shell cat /sdcard/folio_ui.xml | tr '>' '>\n' | grep -qi 'text="Sample"'; then
  BOUNDS=$($ADB shell cat /sdcard/folio_ui.xml | tr '>' '>\n' | grep 'text="Sample"' | head -1 | sed -n 's/.*bounds="\[\([0-9]*\),\([0-9]*\)\]\[\([0-9]*\),\([0-9]*\)\]".*/\1 \2 \3 \4/p')
  read -r L T R B <<<"$BOUNDS"
  X=$(( (L + R) / 2 ))
  Y=$(( (T + B) / 2 ))
  $ADB shell input tap "$X" "$Y"
else
  # Approximate Sample button (right half under toolbar)
  $ADB shell input tap 600 180
fi

sleep 4
FOCUS=$($ADB shell dumpsys window | grep mCurrentFocus || true)
echo "focus: $FOCUS"
echo "$FOCUS" | grep -q ReaderActivity

echo "== RAM while reading =="
$ADB shell dumpsys meminfo "$PKG" | head -25
RSS=$($ADB shell ps -A -o RSS,NAME | grep 'com.pulse.pdf' | awk '{s+=$1} END {print s+0}')
echo "folio_rss_kb=$RSS"

echo "== page label tap (go-to) =="
$ADB shell input tap 400 1180 || true
sleep 1
$ADB shell uiautomator dump /sdcard/folio_ui2.xml >/dev/null
$ADB shell cat /sdcard/folio_ui2.xml | tr '>' '>\n' | grep -E 'text=|content-desc=' | head -20
$ADB shell input keyevent KEYCODE_BACK || true

echo "OK: Folio opened ReaderActivity"
