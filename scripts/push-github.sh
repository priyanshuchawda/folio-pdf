# Publish Folio (PulsePdf) to GitHub as priyanshuchawda — no interactive git config writes.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REPO_NAME="${REPO_NAME:-folio-pdf}"
REPO_DESC="${REPO_DESC:-Folio — ultra-light PDF reader for low-RAM Android tablets (Telegram/Drive, 1000+ pages)}"
AUTHOR_NAME="${AUTHOR_NAME:-priyanshuchawda}"
AUTHOR_EMAIL="${AUTHOR_EMAIL:-priyanshuchawda@users.noreply.github.com}"
BRANCH="${BRANCH:-main}"
VISIBILITY="${VISIBILITY:-public}" # public|private
HOMEPAGE="${HOMEPAGE:-https://folio-pdf-seven.vercel.app}"

export GIT_AUTHOR_NAME="$AUTHOR_NAME"
export GIT_AUTHOR_EMAIL="$AUTHOR_EMAIL"
export GIT_COMMITTER_NAME="$AUTHOR_NAME"
export GIT_COMMITTER_EMAIL="$AUTHOR_EMAIL"

if [[ ! -d .git ]]; then
  git init -b "$BRANCH"
fi

# Ensure .gitignore exists
if [[ ! -f .gitignore ]]; then
  cat > .gitignore <<'EOF'
/.gradle/
/build/
/app/build/
/local.properties
*.iml
.idea/
.DS_Store
/captures/
*.apk
*.ap_
*.dex
*.class
.cxx/
dist/
EOF
fi

# Keep dist/ out of git even if .gitignore already existed without it
if ! grep -qxF 'dist/' .gitignore 2>/dev/null; then
  echo 'dist/' >> .gitignore
fi

git add -A
if git diff --cached --quiet; then
  echo "Nothing new to commit."
else
  git commit --author="${AUTHOR_NAME} <${AUTHOR_EMAIL}>" -m "$(cat <<'EOF'
Docs and release packaging for Folio.

README, license, and GitHub release helpers for the lite tablet PDF reader.
EOF
)"
fi

if ! git remote get-url origin >/dev/null 2>&1; then
  if gh repo view "${AUTHOR_NAME}/${REPO_NAME}" >/dev/null 2>&1; then
    git remote add origin "https://github.com/${AUTHOR_NAME}/${REPO_NAME}.git"
  else
    gh repo create "${AUTHOR_NAME}/${REPO_NAME}" \
      --"${VISIBILITY}" \
      --source=. \
      --remote=origin \
      --description "$REPO_DESC"
  fi
fi

git push -u origin "HEAD:${BRANCH}"

# Keep GitHub About box in sync (non-fatal if offline/permissions differ)
gh repo edit "${AUTHOR_NAME}/${REPO_NAME}" \
  --description "$REPO_DESC" \
  --homepage "$HOMEPAGE" \
  --add-topic android \
  --add-topic pdf \
  --add-topic pdf-reader \
  --add-topic pdfium \
  --add-topic tablet \
  --add-topic fire-tablet \
  --add-topic kotlin \
  --add-topic low-ram \
  >/dev/null 2>&1 || true

echo "Published: https://github.com/${AUTHOR_NAME}/${REPO_NAME}"
