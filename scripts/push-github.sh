#!/usr/bin/env bash
# Publish Folio (PulsePdf) to GitHub as priyanshuchawda — no interactive git config writes.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REPO_NAME="${REPO_NAME:-folio-pdf}"
REPO_DESC="${REPO_DESC:-Folio — ultra-light PDF reader tuned for low-RAM Android tablets}"
AUTHOR_NAME="${AUTHOR_NAME:-priyanshuchawda}"
AUTHOR_EMAIL="${AUTHOR_EMAIL:-priyanshuchawda@users.noreply.github.com}"
BRANCH="${BRANCH:-main}"
VISIBILITY="${VISIBILITY:-public}" # public|private

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
EOF
fi

git add -A
if git diff --cached --quiet; then
  echo "Nothing new to commit."
else
  git commit --author="${AUTHOR_NAME} <${AUTHOR_EMAIL}>" -m "$(cat <<'EOF'
Add Folio: lite tablet PDF reader.

RGB_565 page cache, screen-fit render, short wake lock, and low-RAM ViewPager tuning for Fire HD-class devices.
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
echo "Published: https://github.com/${AUTHOR_NAME}/${REPO_NAME}"
