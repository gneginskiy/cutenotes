#!/usr/bin/env bash
#
# Packages cuteNotes for the operating system it runs on, with a bundled Java runtime (nothing has
# to be installed on the user's machine):
#
#   macOS (Apple silicon)  <out>/cutenotes-mac-arm64.zip       cuteNotes.app
#   Windows x64            <out>/cutenotes-windows-x64.zip     cuteNotes\cuteNotes.exe
#   Linux x64              <out>/cutenotes-linux-x64.tar.gz    cuteNotes/bin/cuteNotes
#
# jpackage cannot build for another OS, so CI runs this on a macOS, a Windows and a Linux runner
# (.github/workflows/release.yml). Locally it builds the package of your own machine.
#
# Usage: scripts/package.sh [jar, default target/cutenotes.jar] [out dir, default target/dist]

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAR_ARG="${1:-$ROOT/target/cutenotes.jar}"
JAR="$(cd "$(dirname "$JAR_ARG")" && pwd)/$(basename "$JAR_ARG")"
OUT="${2:-$ROOT/target/dist}"
MODULES="java.base,java.desktop"
JLINK_OPTS="--strip-debug --no-man-pages --no-header-files --compress=zip-6"
LAUNCH_OPTS="-Dcutenotes.installed=true"
JPACKAGE="${JAVA_HOME:+$JAVA_HOME/bin/}jpackage"

case "$(uname -s)-$(uname -m)" in
  Darwin-arm64) PLATFORM=mac-arm64 ;;
  MINGW*-x86_64 | MSYS*-x86_64 | CYGWIN*-x86_64) PLATFORM=windows-x64 ;;
  Linux-x86_64) PLATFORM=linux-x64 ;;
  *) echo "Unsupported platform: $(uname -s)-$(uname -m)" >&2; exit 1 ;;
esac

[[ -f "$JAR" ]] || { echo "No jar at $JAR: run mvn clean install first." >&2; exit 1; }
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/input" "$WORK/image"
cp "$JAR" "$WORK/input/cutenotes.jar"

# jpackage wants a numeric version; Windows caps the first two parts at 255, hence YY.M.D.
VERSION="$(date -u +%Y-%m-%d | awk -F- '{printf "%d.%d.%d", $1 - 2000, $2, $3}')"

cd "$WORK"
extra=()
if [[ "$PLATFORM" == mac-* ]]; then
  extra=(--mac-package-identifier cutenotes.app --mac-package-name cuteNotes)
fi
"$JPACKAGE" --type app-image --dest image --name cuteNotes --app-version "$VERSION" \
  --vendor "Grigorii Neginskii" --input input --main-jar cutenotes.jar \
  --main-class cutenotes.Main --java-options "$LAUNCH_OPTS" \
  --add-modules "$MODULES" --jlink-options "$JLINK_OPTS" "${extra[@]}"

readme() {
  cat <<EOF
cuteNotes ($PLATFORM)

$1

No Java installation is needed: the app ships with its own runtime.
Notes are stored in the "cutenotes_data" folder in your home folder, so
replacing the app with a newer version keeps them.
Coming from the plain .jar? Copy the "cutenotes_data" folder that sits next to
the old jar into your home folder.
EOF
}

case "$PLATFORM" in
  mac-arm64)
    codesign --verify --deep --strict image/cuteNotes.app
    readme "Unzip, move cuteNotes.app to Applications and open it. The app is not
notarized: on the first start macOS asks for confirmation
(System Settings > Privacy & Security > Open Anyway)." > image/README.txt
    ARCHIVE="$OUT/cutenotes-$PLATFORM.zip"
    rm -f "$ARCHIVE"
    (cd image && ditto -c -k --norsrc --keepParent cuteNotes.app "$ARCHIVE" \
      && zip -q -X "$ARCHIVE" README.txt)
    ;;
  windows-x64)
    readme "Unzip anywhere (e.g. your user folder) and start cuteNotes\\cuteNotes.exe." \
      | sed 's/$/\r/' > image/cuteNotes/README.txt
    ARCHIVE="$OUT/cutenotes-$PLATFORM.zip"
    rm -f "$ARCHIVE"
    (cd image && 7z a -tzip -bso0 -bsp0 "$ARCHIVE" cuteNotes)
    ;;
  linux-x64)
    readme "Unpack (tar -xzf) and run cuteNotes/bin/cuteNotes. Needs a desktop session
(X11 or XWayland) with the usual X, fontconfig and freetype libraries." \
      > image/cuteNotes/README.txt
    ARCHIVE="$OUT/cutenotes-$PLATFORM.tar.gz"
    tar -czf "$ARCHIVE" -C image cuteNotes
    ;;
esac

echo "$ARCHIVE"
