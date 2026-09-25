#!/usr/bin/env bash
#
# Builds self-contained cuteNotes releases: nothing (no JDK, no JRE) has to be installed on the
# target machine.
#
#   releases/mac-arm/cutenotes-<date>-mac-arm64.zip      cuteNotes.app (jpackage, bundled runtime)
#   releases/windows/cutenotes-<date>-windows-x64.zip    portable folder, start cuteNotes.bat
#   releases/linux/cutenotes-<date>-linux-x64.tar.gz     portable folder, start ./cutenotes
#   releases/cutenotes-<date>.jar                        the plain jar (needs Java 21+)
#
# Run it on an Apple-silicon Mac with the Corretto JDK used for the build. jpackage cannot build
# for another OS, so the Windows and Linux runtimes are cross-linked with the local jlink from the
# jmods of the same Corretto version for that platform (downloaded once and cached).
#
# Usage: scripts/build-releases.sh [release id, default: today as YYYY-MM-DD; a second release
#        on the same day gets a suffix, e.g. 2026-09-26-1]

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DATE="${1:-$(date +%F)}"
MODULES="java.base,java.desktop"
JLINK_OPTS=(--strip-debug --no-man-pages --no-header-files --compress=zip-6)
LAUNCH_OPTS="-Dcutenotes.installed=true"
CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/cutenotes-jdks"
WORK="$ROOT/target/packaging"

JDK_HOME="${JDK_HOME:-$(/usr/libexec/java_home)}"
CORRETTO="$(sed -n 's/^IMPLEMENTOR_VERSION="Corretto-\(.*\)"/\1/p' "$JDK_HOME/release")"
if [[ -z "$CORRETTO" ]]; then
  echo "The local JDK must be Amazon Corretto: jlink only links jmods of its own version." >&2
  exit 1
fi
if [[ "$(uname -s)-$(uname -m)" != "Darwin-arm64" ]]; then
  echo "Run on an Apple-silicon Mac: the macOS app is built natively with jpackage." >&2
  exit 1
fi

step() { printf '\n==> %s\n' "$*"; }

# Downloads and unpacks the Corretto JDK of the local version for <platform> (linux-x64 | windows-x64);
# prints its home directory.
target_jdk() {
  local platform="$1" archive url dir
  if [[ "$platform" == windows-* ]]; then
    archive="amazon-corretto-$CORRETTO-$platform-jdk.zip"
  else
    archive="amazon-corretto-$CORRETTO-$platform.tar.gz"
  fi
  url="https://corretto.aws/downloads/resources/$CORRETTO/$archive"
  dir="$CACHE/$CORRETTO-$platform"
  if [[ ! -d "$dir" ]]; then
    mkdir -p "$CACHE"
    echo "downloading $url" >&2
    curl -fsSL --retry 3 -o "$CACHE/$archive" "$url"
    mkdir -p "$dir.tmp"
    if [[ "$archive" == *.zip ]]; then
      unzip -q "$CACHE/$archive" -d "$dir.tmp"
    else
      tar -xzf "$CACHE/$archive" -C "$dir.tmp"
    fi
    mv "$dir.tmp" "$dir"
    rm -f "$CACHE/$archive"
  fi
  dirname "$(find "$dir" -maxdepth 3 -type d -name jmods | head -1)"
}

# Links a minimal runtime for another platform from that platform's jmods.
cross_runtime() {
  local jdk="$1" out="$2"
  rm -rf "$out"
  "$JDK_HOME/bin/jlink" --module-path "$jdk/jmods" --add-modules "$MODULES" \
    "${JLINK_OPTS[@]}" --output "$out"
}

notes_txt() {
  cat <<EOF
cuteNotes $DATE ($1)

$2

No Java installation is needed: the app ships with its own runtime.
Your notes are stored in the "cutenotes_data" folder in your home folder, so
replacing this app with a newer version keeps them.
Upgrading from the plain .jar? Copy the "cutenotes_data" folder that sits next
to the old jar into your home folder.
EOF
}

step "Building the jar (mvn clean install)"
(cd "$ROOT" && mvn -q clean install)
rm -rf "$WORK" && mkdir -p "$WORK/input"
cp "$ROOT/target/cutenotes.jar" "$WORK/input/"
JAR="$ROOT/releases/cutenotes-$DATE.jar"
cp "$ROOT/target/cutenotes.jar" "$JAR"
APP_VERSION="$(echo "$DATE" | awk -F- '{printf "%d.%d.%d", $1, $2, $3}')"

step "macOS (Apple silicon): cuteNotes.app"
"$JDK_HOME/bin/jpackage" --type app-image --dest "$WORK/mac" \
  --name cuteNotes --app-version "$APP_VERSION" --vendor "Grigorii Neginskii" \
  --input "$WORK/input" --main-jar cutenotes.jar --main-class cutenotes.Main \
  --java-options "$LAUNCH_OPTS" --add-modules "$MODULES" --jlink-options "${JLINK_OPTS[*]}" \
  --mac-package-identifier cutenotes.app --mac-package-name cuteNotes
codesign --verify --deep --strict "$WORK/mac/cuteNotes.app"
notes_txt "macOS, Apple silicon" \
  "Unzip, move cuteNotes.app to Applications and open it. The app is not notarized:
on the first start macOS asks for confirmation (System Settings > Privacy &
Security > Open Anyway)." > "$WORK/mac/README.txt"
mkdir -p "$ROOT/releases/mac-arm"
MAC_ZIP="$ROOT/releases/mac-arm/cutenotes-$DATE-mac-arm64.zip"
rm -f "$MAC_ZIP"
(cd "$WORK/mac" && ditto -c -k --norsrc --keepParent cuteNotes.app "$MAC_ZIP" \
  && zip -q -X "$MAC_ZIP" README.txt)

step "Windows x64: portable folder"
WIN="$WORK/windows/cuteNotes"
mkdir -p "$WIN/app"
cross_runtime "$(target_jdk windows-x64)" "$WIN/runtime"
cp "$WORK/input/cutenotes.jar" "$WIN/app/"
printf '@echo off\r\nstart "" "%%~dp0runtime\\bin\\javaw.exe" %s -jar "%%~dp0app\\cutenotes.jar" %%*\r\n' \
  "$LAUNCH_OPTS" > "$WIN/cuteNotes.bat"
notes_txt "Windows x64" \
  "Unzip anywhere (e.g. your user folder) and double-click cuteNotes.bat." \
  | sed 's/$/\r/' > "$WIN/README.txt"
mkdir -p "$ROOT/releases/windows"
WIN_ZIP="$ROOT/releases/windows/cutenotes-$DATE-windows-x64.zip"
rm -f "$WIN_ZIP"
(cd "$WORK/windows" && zip -q -r -X "$WIN_ZIP" cuteNotes)

step "Linux x64: portable folder"
LIN="$WORK/linux/cutenotes"
mkdir -p "$LIN/app"
cross_runtime "$(target_jdk linux-x64)" "$LIN/runtime"
cp "$WORK/input/cutenotes.jar" "$LIN/app/"
cat > "$LIN/cutenotes" <<EOF
#!/bin/sh
DIR="\$(cd "\$(dirname "\$0")" && pwd)"
exec "\$DIR/runtime/bin/java" $LAUNCH_OPTS -jar "\$DIR/app/cutenotes.jar" "\$@"
EOF
chmod +x "$LIN/cutenotes"
notes_txt "Linux x64" \
  "Unpack (tar -xzf) and run ./cutenotes. Needs a desktop session (X11 or
XWayland); the usual X, fontconfig and freetype libraries of any desktop
distribution are enough." > "$LIN/README.txt"
mkdir -p "$ROOT/releases/linux"
LIN_TAR="$ROOT/releases/linux/cutenotes-$DATE-linux-x64.tar.gz"
(cd "$WORK/linux" && COPYFILE_DISABLE=1 tar --no-mac-metadata --no-xattrs -czf "$LIN_TAR" cutenotes)

step "Done"
ls -lh "$JAR" "$MAC_ZIP" "$WIN_ZIP" "$LIN_TAR"
