#!/usr/bin/env bash
#
# Starts a native package and checks that the app comes up and keeps running without errors: on
# Linux on a virtual display, on macOS and Windows on the runner's desktop. Used by CI right after
# packaging.
#
# Usage: scripts/smoke-test.sh <cutenotes-<platform>.zip | .tar.gz>

set -euo pipefail

ARCHIVE="$1"
SECONDS_UP=20
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
case "$(basename "$ARCHIVE")" in
  *linux*)
    tar -xzf "$ARCHIVE" -C "$TMP"
    RUN=(xvfb-run -a "$TMP/cuteNotes/bin/cuteNotes") ;;
  *mac*)
    ditto -x -k "$ARCHIVE" "$TMP"
    RUN=("$TMP/cuteNotes.app/Contents/MacOS/cuteNotes") ;;
  *windows*)
    # 7-Zip is a Windows program: "-o<dir>" is not a path Git Bash converts by itself.
    7z x -bso0 -bsp0 "-o$(cygpath -w "$TMP")" "$ARCHIVE"
    RUN=("$TMP/cuteNotes/cuteNotes.exe") ;;
  *) echo "Unknown package: $ARCHIVE" >&2; exit 1 ;;
esac

# Java takes user.home from the account database, not from $HOME: point it at a fresh folder.
HOME_DIR="$TMP/home"
mkdir -p "$HOME_DIR"
JAVA_HOME_DIR="$HOME_DIR"
command -v cygpath > /dev/null && JAVA_HOME_DIR="$(cygpath -w "$HOME_DIR")"
export JAVA_TOOL_OPTIONS="-Duser.home=$JAVA_HOME_DIR"

set +e
timeout "$SECONDS_UP" "${RUN[@]}" > "$TMP/app.log" 2>&1
status=$?
set -e

cat "$TMP/app.log"
DATA="$HOME_DIR/cutenotes_data"
cat "$DATA"/logs/*.log 2> /dev/null || true
if [[ $status -ne 124 ]]; then
  echo "cuteNotes exited early with status $status" >&2
  exit 1
fi
if grep -qs "Exception\|SEVERE" "$TMP/app.log" "$DATA"/logs/*.log; then
  echo "cuteNotes logged an error" >&2
  exit 1
fi
[[ -d "$DATA" ]] || { echo "no data folder in the home folder" >&2; exit 1; }
echo "Smoke test passed: the app ran for $SECONDS_UP s and keeps its notes in ~/cutenotes_data."
