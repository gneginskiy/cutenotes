# cuteNotes: builds on GitHub Actions, downloads on GitHub Releases

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 189 tests pass, 0 checkstyle violations, coverage above the threshold.

## Why

Each release added about 100 MB of platform archives to git. Every clone downloaded every old build, and the history grew forever. The Windows and Linux builds were also cross-linked on a Mac and had never run on real Windows or Linux.

## What

| Before | Now |
|--------|-----|
| Archives and jars committed to `releases/` | [GitHub Releases](https://github.com/gneginskiy/cutenotes/releases); binaries are never committed (`.gitignore`) |
| Built by hand on an Apple-silicon Mac; Windows/Linux runtimes cross-linked from downloaded Corretto jmods | `.github/workflows/release.yml` on **every push to `develop`**: jar + tests on Ubuntu, then jpackage natively on `macos-14`, `windows-latest` and `ubuntu-latest` |
| Windows: `cuteNotes.bat` (a console window flashed) | `cuteNotes\cuteNotes.exe`, a real jpackage launcher |
| Linux: `cutenotes/cutenotes` shell script | `cuteNotes/bin/cuteNotes` |
| Linux build checked by hand in Docker | CI smoke test: the app runs 20 s under Xvfb, with no exception in the log and notes in `~/cutenotes_data` |
| README linked to files in the repository by version | README links to `releases/latest/download/<file>` (stable asset names), so it never goes stale |

## How

- `scripts/package.sh [jar] [out]` packages for the OS it runs on. CI calls it on each runner, and locally it builds the package of your own machine.
- `scripts/smoke-test-linux.sh` runs the Linux smoke test.
- `scripts/publish-release.sh` creates the release. The tag is the commit date in the author's time zone, with `-1`, `-2`… on the same day. Ids already taken by the committed jars are skipped, so the numbering continues (this build is `v2026-09-26-3`). The release notes are the commit message.
- Assets: `cutenotes.jar`, `cutenotes-mac-arm64.zip`, `cutenotes-windows-x64.zip`, `cutenotes-linux-x64.tar.gz`.
- Releases are published one at a time (`concurrency`), and a failing test or smoke test publishes nothing.
- Cost: nothing. GitHub Releases are free, and so is Actions for a public repository, including macOS runners.

## History

The platform archives (`releases/mac-arm`, `releases/windows`, `releases/linux`) were removed from the two commits that contained them (`git filter-branch --index-filter`), and `develop` was force-pushed. Everything else in those commits stayed, including the jars. Older commits kept their hashes. The jars committed before this change stay in `releases/`; new jars go only to GitHub Releases.

Removed: `scripts/build-releases.sh` (the cross-linking build). Added: `.github/workflows/release.yml`, `scripts/package.sh`, `scripts/smoke-test-linux.sh`, `scripts/publish-release.sh`.
