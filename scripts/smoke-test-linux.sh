#!/usr/bin/env bash
#
# Starts the Linux package on a virtual display and checks that the window comes up and keeps
# running without errors. Used by CI right after packaging.
#
# Usage: scripts/smoke-test-linux.sh <cutenotes-linux-x64.tar.gz>

set -euo pipefail

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
tar -xzf "$1" -C "$TMP"
# Java takes user.home from the account database, not from $HOME: point it at a fresh folder.
HOME_DIR="$TMP/home"
mkdir -p "$HOME_DIR"
export JAVA_TOOL_OPTIONS="-Duser.home=$HOME_DIR"

set +e
timeout 20 xvfb-run -a "$TMP/cuteNotes/bin/cuteNotes" > "$TMP/app.log" 2>&1
status=$?
set -e

cat "$TMP/app.log"
if [[ $status -ne 124 ]]; then
  echo "cuteNotes exited early with status $status" >&2
  exit 1
fi
if grep -q "Exception" "$TMP/app.log"; then
  echo "cuteNotes logged an exception" >&2
  exit 1
fi
[[ -d "$HOME_DIR/cutenotes_data" ]] || { echo "no data folder in the home folder" >&2; exit 1; }
echo "Smoke test passed: the app ran for 20 s and keeps its notes in ~/cutenotes_data."
