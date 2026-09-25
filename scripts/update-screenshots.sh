#!/usr/bin/env bash
#
# Re-renders the README screenshots (screenshots/img*.png) from the current code: the real UI
# components of the app in a drawn macOS window frame, at Retina resolution. Nothing appears on
# screen and no notes are touched. img6.png shows the current README.md pasted into a note.
#
# Run on a Mac (the native Aqua look needs the WindowServer).
#
# Usage: scripts/update-screenshots.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/target/screenshots-tool"

(cd "$ROOT" && mvn -q -DskipTests compile)
rm -rf "$OUT" && mkdir -p "$OUT"
javac -d "$OUT" -cp "$ROOT/target/classes" "$ROOT/scripts/screenshots/view/ReadmeScreenshots.java"
java -Dapple.awt.UIElement=true -cp "$ROOT/target/classes:$OUT" view.ReadmeScreenshots \
  "$ROOT/screenshots" "$ROOT/README.md"
