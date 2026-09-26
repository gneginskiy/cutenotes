# cuteNotes: product review implemented

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 333 tests pass, 0 checkstyle violations, coverage above the threshold.

Everything from the [product review](2026-09-26-product-review.md) is implemented except the release process (6.9: pre-release vs "Latest"), which was left out on purpose, and a few items listed at the end.
Every bug fix started with a test that reproduced it.

## Summary

| Area | What changed |
|---|---|
| First start | Hint in an empty note, welcome / "updated" toast, File · Edit · Format · View · Help menus, a text context menu |
| Quick capture | Starting the app again shows or hides the window (bind any OS hotkey to it), Dock reopen on macOS, tray icon, drag & drop of files, app icon |
| Editor | Lists and checkboxes continue on Enter, `Tab` / `Shift+Tab` indent, headings, done tasks struck through, clickable links, `#tags`, code blocks, find & replace (case, whole word, regex), paste as plain text, word count, line width, automatic titles |
| Search and organisation | Full-text search with snippets in `Cmd/Ctrl+R`, tag filter, pinned notes, note colours, sort (manual / recent / name), recent notes, `Cmd/Ctrl+1…9`, previous tab |
| Data safety | Recently deleted (30 days), version history, daily backup, choice of notes folder, reload or keep both when a file changes outside, a warning when another computer uses the folder, password-protected notes with auto-lock, export (md / txt / html) and print |
| Look | Muted text meets WCAG AA on every preset, follow the system's light / dark mode, macOS title-bar hint, image spacing, sharper image scaling |
| Accessibility, language | Russian translation (language from the OS or chosen in Options), screen-reader names for icon buttons and tabs |
| Distribution | About window, version built into the app, daily update check, logs, installers (.dmg / .msi / .deb), macOS Intel and Linux arm64 builds, smoke tests on every OS |
| Bugs from section 7 | Image paths confined to the data folder, unused images collected, fast image scaling, external changes no longer overwritten, even image spacing, version from the commit date, title bar |

---

## Data safety

**Recently deleted.** Deleting a note moves its file to `cutenotes_data/trash` (no question asked, a toast says where it went).
File › Recently deleted… and a bin button in the notes browser list them with a preview: Restore, Delete forever, Empty. Notes older than 30 days are removed at start.
`TrashBin` / `FileTrashBin`, `TabRepository.trash`, `TrashDialog`. Tests: `TrashBinTest`, `NoteSessionTest`.

**Version history.** `HistoryRepository` records a save in `cutenotes_data/history/<id>/` at most every 10 minutes, only when the text changed, keeping 50 per note.
File › Version history… shows them; restoring first records the current text and replaces it as one undo step.
Protected notes are never recorded. `HistoryStore`, `NoteHistory`, `HistoryDialog`. Tests: `HistoryStoreTest`.

**Backups and housekeeping.** At start, on a virtual thread: a zip a day in `cutenotes_data/backups` (the last 7), the bin emptied of old notes, unused images removed. Images referenced from the bin or the history are kept.
`Backups`, `DataFiles`, `Housekeeping`, `NoteStorage`. Tests: `BackupsTest`, `DataFilesTest`, `HousekeepingTest`, `NoteStorageTest`.

**Files changed outside the app.** Every 2 s the open notes' file times are compared. Without unsaved edits the tab shows the new text (undoable). With unsaved edits the tab keeps its text and the disk version is saved as "‹name› (from disk)".
`ModTimes`, `DiskChange`, `ExternalEdits`, `AutoSaver.lastSaved`. Tests: `DiskChangeTest`, `ExternalEditsTest`.

**Shared folders.** `.cutenotes-owner` holds the host name and time, renewed every minute. A start within 3 minutes of another computer asks before opening. `FolderLease`. Tests: `FolderLeaseTest`.

**Password protection.** File › Protect with password… encrypts the note (AES-256-GCM, PBKDF2) and deletes its history. A protected note asks for its password when it is opened. File › Lock protected notes (`Cmd/Ctrl+Shift+L`) and the idle timer from Options close the unlocked notes and forget the keys.
`EncryptingRepository` is the outermost layer, so neither the history nor the disk sees plain text. It never overwrites a protected note with plain text; only "Remove password…" (which asks for the password again) does that.
`Keyring`, `NoteLocks`, `PasswordPrompt`, `AutoLock`. Tests: `EncryptingRepositoryTest`, `NoteSessionTest`.

**Export and print.** File › Export… (`Cmd/Ctrl+Shift+E`) writes Markdown, plain text or a web page with headings, lists, checkboxes, styles, links and images. File › Print… (`Cmd/Ctrl+P`) prints on white paper in the note's font.
`HtmlExport`, `MdLines`, `ExportFormat`, `NoteExport`. Tests: `HtmlExportTest`, `ExportFormatTest`.

## Options › Application

A second page in Options; every change applies and is saved at once (`SettingsHolder`, `cutenotes_settings.txt`):
- Language: system, English or Russian, applied after a restart.
- Follow the system's light / dark mode, with a light and a dark preset. The system is asked via `defaults`, the registry or `gsettings`.
- Line width: full window or 60–120 characters, centred.
- Check for updates, tray icon, auto-lock minutes.
- Notes folder: pick one (e.g. in Dropbox), optionally copy the current notes, used after a restart.

`AppSettingsPanel`, `SettingControls`, `ThemeFollower`, `SystemDarkMode`, `FolderChooser`, `AppTray`. Tests: `SystemDarkModeTest`.

## Quick capture

**Starting cuteNotes again** shows or hides the running window. The running app listens on a local port written, with a random token, to `.cutenotes-port`. A system hotkey bound to starting the app (see README) becomes a global show/hide hotkey without native code.
`InstanceChannel`, `WindowReopen`. Tests: `InstanceChannelTest`.

**Drag & drop.** `.txt` / `.md` files open as new notes; pictures are inserted where they are dropped. Other files are explained in a toast. Dragging text inside a note still works.
`FileDrop`, `DroppedFile`. Tests: `FileDropTest`, `DroppedFileTest`.

## Accessibility

Muted text blended from the theme was below WCAG AA (Sepia 2.76 : 1 on the bars). It is now darkened just enough to reach 4.5 : 1 on the page and the bars (`Colors.legible`). Tests: `PaletteContrastTest`.
Icon buttons and tabs have names for screen readers. Tests: `AccessibleNamesTest`.

## Packaging and CI

- The runtime now includes `java.logging` and `java.net.http`: without them the native builds could not write logs or check for updates. It also includes `jdk.localedata` (Russian dates only) and `jdk.accessibility`.
- The app icon for the packages is drawn by the app (`scripts/icons/IconFiles.java`): `.icns`, `.ico`, `.png`.
- The release tag is computed once (`scripts/release-tag.sh`) and built into the jar, so the app shows the same version as the release. The package version comes from the commit date.
- Installers (`INSTALLERS=1 scripts/package.sh`): `.dmg`, `.msi`, `.deb`.
- New experimental platforms: macOS Intel and Linux arm64.
- `scripts/smoke-test.sh` smoke-tests every package. It is required on Linux; on macOS and Windows it does not block yet.
- Checked locally: package, `.dmg` and the modules of the runtime on macOS arm64.

## Refactoring

- `ForwardingRepository` (shared by the history and encryption decorators).
- `ListPreviewDialog` (shared by the bin and the history).
- `DataFiles` (shared by backups and the folder copy).
- `RecentMenu`, `SettingControls` (split out to stay within checkstyle limits).
- Lombok `@With` on `AppSettings`, `@RequiredArgsConstructor` on `AutoSaver`.
- A large-note budget test: 1 MB opens and reads back in about 0.9 s (limit 10 s). Test: `LargeNoteTest`.

## Not done, and why

| Item | Reason |
|---|---|
| Release process (6.9) | Excluded by request |
| macOS notarization, Windows signing | Need an Apple Developer ID and a signing certificate |
| Homebrew / winget / Flathub, product page | Need accounts and hosting decisions |
| Windows ARM64 | No reliable JDK + jpackage combination on the hosted runner yet |
| Spell checking | Needs a dictionary library (dependency policy) |
| Coverage threshold for `view/*` | Coverage settings must not be changed |
| Screenshot comparison tests | Pixel comparisons across fonts and OS versions would be flaky |
| Formatting bar over the selection, compact density, window opacity, tab-bar arrow-key navigation | P3; the menus, context menu and shortcuts already cover them |
| Old notes with unescaped `*` / `__` | P3; ambiguous to fix automatically without touching valid formatting |
