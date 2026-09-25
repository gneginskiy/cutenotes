#!/usr/bin/env bash
#
# Publishes the built files as a GitHub Release of the current commit. The tag is the commit's date
# in its author's time zone, with -1, -2… for further releases on the same day (v2026-09-26,
# v2026-09-26-1, …). Ids already used by the jars committed in releases/ (cutenotes-<id>.jar, from
# before releases moved to GitHub) are skipped, so the numbering continues. Asset names carry no
# version, so README links to releases/latest/download/<file> always get the newest build.
#
# Needs GH_TOKEN (contents: write) and GITHUB_SHA. Used by .github/workflows/release.yml.
#
# Usage: scripts/publish-release.sh <dir with the files>

set -euo pipefail

DIST="$1"
DATE="$(git log -1 --date=format:%Y-%m-%d --format=%ad "$GITHUB_SHA")"
TAG="v$DATE"
n=0
while [[ -f "releases/cutenotes-${TAG#v}.jar" ]] || gh release view "$TAG" > /dev/null 2>&1; do
  n=$((n + 1))
  TAG="v$DATE-$n"
done

NOTES="$(mktemp)"
{
  git log -1 --format=%B "$GITHUB_SHA"
  echo
  echo "---"
  echo "Built from ${GITHUB_SHA:0:7} on develop."
  echo "macOS (Apple silicon), Windows x64 and Linux x64 builds bundle their own Java runtime;"
  echo "the .jar needs Java 21+. Release notes: docs/release_notes."
} > "$NOTES"

gh release create "$TAG" "$DIST"/* \
  --target "$GITHUB_SHA" \
  --title "cuteNotes ${TAG#v}" \
  --notes-file "$NOTES" \
  --latest
echo "Published $TAG"
