#!/usr/bin/env bash
#
# Publishes the built files as a GitHub Release of the current commit, tagged $TAG (the tag the jar
# was built with) or else the next tag from scripts/release-tag.sh. Asset names carry no version,
# so README links to releases/latest/download/<file> always get the newest build.
#
# Needs GH_TOKEN (contents: write) and GITHUB_SHA. Used by .github/workflows/release.yml.
#
# Usage: scripts/publish-release.sh <dir with the files>

set -euo pipefail

DIST="$1"
TAG="${TAG:-$("$(dirname "$0")/release-tag.sh" "$GITHUB_SHA")}"

NOTES="$(mktemp)"
{
  git log -1 --format=%B "$GITHUB_SHA"
  echo
  echo "---"
  echo "Built from ${GITHUB_SHA:0:7} on develop."
  echo "The macOS, Windows and Linux builds and installers bundle their own Java runtime;"
  echo "the .jar needs Java 21+. Release notes: docs/release_notes."
} > "$NOTES"

gh release create "$TAG" "$DIST"/* \
  --target "$GITHUB_SHA" \
  --title "cuteNotes ${TAG#v}" \
  --notes-file "$NOTES" \
  --latest
echo "Published $TAG"
