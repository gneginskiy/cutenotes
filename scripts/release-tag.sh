#!/usr/bin/env bash
#
# Prints the tag of the next GitHub Release of a commit: the commit's date in its author's time
# zone, with -1, -2… for further releases on the same day (v2026-09-26, v2026-09-26-1, …). Ids
# already used by the jars committed in releases/ (cutenotes-<id>.jar, from before releases moved
# to GitHub) are skipped, so the numbering continues.
#
# Needs GH_TOKEN. Usage: scripts/release-tag.sh [commit, default HEAD]

set -euo pipefail

DATE="$(git log -1 --date=format:%Y-%m-%d --format=%ad "${1:-HEAD}")"
TAG="v$DATE"
n=0
while [[ -f "releases/cutenotes-${TAG#v}.jar" ]] || gh release view "$TAG" > /dev/null 2>&1; do
  n=$((n + 1))
  TAG="v$DATE-$n"
done
echo "$TAG"
