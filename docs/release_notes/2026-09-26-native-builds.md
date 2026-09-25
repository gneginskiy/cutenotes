# cuteNotes: native builds without a JDK (macOS arm, Windows x64, Linux x64)

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 187 tests pass, 0 checkstyle violations, coverage above the threshold.

## What

| Folder | File | Contents | How to start |
|--------|------|----------|--------------|
| `releases/mac-arm/` | `cutenotes-<date>-mac-arm64.zip` (~32 MB) | `cuteNotes.app` built with jpackage, bundled runtime, ad-hoc signature | Move to Applications and open it |
| `releases/windows/` | `cutenotes-<date>-windows-x64.zip` (~32 MB) | Portable folder: `runtime/`, `app/cutenotes.jar`, `cuteNotes.bat` | `cuteNotes.bat` (runs `javaw`, no console window stays open) |
| `releases/linux/` | `cutenotes-<date>-linux-x64.tar.gz` (~36 MB) | Portable folder: `runtime/`, `app/cutenotes.jar`, `cutenotes` script | `./cutenotes` |

Each bundle has a `README.txt`. The runtime contains only `java.base` and `java.desktop` (plus their dependencies), built with `--strip-debug`, `--no-man-pages` and `--compress=zip-6`.

## How

- `scripts/build-releases.sh <YYYY-MM-DD>` builds the jar (`mvn clean install`) and all three bundles.
- jpackage cannot build for another OS. So the macOS app is built natively on Apple silicon, and the Windows and Linux runtimes are **cross-linked**: the local `jlink` links the jmods of the same Corretto version (25.0.1.8.1) for that platform. The version is taken from the local JDK, and the archives are downloaded once into `~/.cache/cutenotes-jdks`.
- The launchers pass `-Dcutenotes.installed=true`.

## Where the notes live (bug prevented)

In a packaged app the jar sits inside the app itself (`cuteNotes.app/Contents/app/`). With the old rule ("data next to the jar"), replacing the app on upgrade would have deleted every note. `DataDir.pick` now stores the data of installed apps in `~/cutenotes_data`, the same folder that was already the fallback. The plain jar stays portable and keeps its data next to itself.

Tests (written first): `DataDirTest.anInstalledAppKeepsNotesInTheHomeFolderNotInsideItself`, `theBareJarStaysPortable`.

## Verification

- **macOS:** the launcher is `Mach-O arm64`, `codesign --verify --deep --strict` passes (ad-hoc signature), and the runtime lists 5 modules.
- **Windows:** `javaw.exe` is `PE32+ (GUI) x86-64`, and the `.bat` has CRLF line endings. It was not run on a real Windows machine.
- **Linux:** the tarball was unpacked in Docker `debian:bookworm-slim --platform linux/amd64` with Xvfb. The window opened, text typed with `xdotool` rendered in DejaVu Sans, `Esc` showed the menu, the notes went to `~/cutenotes_data`, and the log had no errors.

## Limitations

- The macOS app is not notarized (that needs an Apple Developer ID), so the first start asks for confirmation: Privacy & Security → Open Anyway.
- The Windows build is a `.bat` without an `.exe` or an icon. A real installer (`.msi`/`.exe`) can only be built by jpackage on Windows. Unsigned files may trigger SmartScreen.
- None of the apps has its own icon yet (they use the default Java icon).
- Each bundle adds about 32–36 MB to git; for large histories GitHub Releases or Git LFS would be better.
