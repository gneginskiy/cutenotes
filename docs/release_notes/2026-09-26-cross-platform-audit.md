# cuteNotes: cross-platform audit (macOS, Windows, Linux)

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 173 tests pass, 0 checkstyle violations, coverage above the threshold.

I checked every shortcut, mouse gesture, OS integration and file-system assumption against all three platforms. For each problem, a test that reproduces it was written first.

## Already cross-platform (verified, unchanged)

- **Modifiers.** Every shortcut goes through `ShortcutMask.menu()`: `Cmd` on macOS, `Ctrl` elsewhere. The menu shows accelerators in the platform's own notation.
- **Context menus** check `isPopupTrigger()` on both press and release. That covers macOS/Linux (press), Windows (release) and `Ctrl+click` on macOS.
- **macOS-only APIs** are guarded: the title-bar tint and appearance run only on macOS, and `QuitStrategy` / `Desktop.open` are called only after `isSupported`.
- **Files.** File names drop the characters Windows forbids (`\ / : * ? " < > |`) and control characters; a name can never be a reserved device name because every file starts with `note_`. Image paths are resolved with `Path.resolve`, which accepts `/` on Windows. Line endings are read with `\R`. The data folder falls back to the home folder when the jar sits in `Program Files` or `/Applications`.
- **HiDPI.** Icons are vector shapes (no emoji), so they stay sharp at any Windows/Linux scale factor.

## Fixed

| # | Problem | Platform | Fix |
|---|---------|----------|-----|
| 1 | "Next tab" was also bound to `Alt+Tab` and shown in F1, but the OS window switcher takes that key | Windows, Linux | `Option+Tab` only on macOS; `Ctrl+Tab` everywhere |
| 2 | Back / forward was `Ctrl+Alt+←/→`: it switches workspaces on GNOME and rotates the screen with some Intel drivers on Windows | Windows, Linux | `Alt+←/→`, as in browsers; `Ctrl+Alt` is kept as a second binding; macOS stays `Cmd+Option+←/→` |
| 3 | Zoom only worked as `Cmd/Ctrl+Shift+=`: on German, French and other layouts `=` is a different key, so zooming in was impossible | all | Also `Cmd/Ctrl+=`, `Cmd/Ctrl++` and the numpad `+`/`-`; the Shift variants still work |
| 4 | Code sections used Java's logical `Monospaced`, which is Courier New on Windows | Windows (and Linux) | The first installed of SF Mono / JetBrains Mono / Menlo / Cascadia Mono / Consolas / DejaVu Sans Mono |
| 5 | With the new default font ("SF Pro Text"), text un-coded or recoloured after a theme change got that uninstalled font name and was drawn in Java's fallback font | Windows, Linux | `EditorFormat.styleCode` resolves the family through `FontFamilies.resolve` |
| 6 | Java defaulted to the Metal look (bold labels, 2005-era dialogs); only macOS was native | Windows, Linux | Windows: the system look. Linux: Metal without bold fonts (the GTK look ignores the theme colours of menus, trees and fields) |
| 7 | Hand-drawn chrome (tabs, find bar, toast, help) used Metal's logical font `Dialog` on Linux | Linux | `UiFonts` replaces logical fonts with the best installed UI font (Noto Sans, Ubuntu, Cantarell, DejaVu Sans…) |
| 8 | `awt.useSystemAAFontSettings=lcd` overrode ClearType settings on Windows and forced subpixel AA on HiDPI and BGR panels | Windows, Linux | Not set on Windows (Java already reads ClearType); `on` (grayscale) on Linux |

The look-and-feel is set before the first dialog. The "already running" message is shown after the theme is loaded, so it is native as well.

## How

A new `PlatformKeys` class holds every shortcut that differs by platform: next-tab modifiers, navigation modifiers, zoom key variants and their help texts. `AppMenu`, `CaretHistory`, `Zoom` and `HelpRows` all read from it, so a binding and its F1 text cannot drift apart. `HelpRows` now takes the platform flag instead of pre-built `Cmd`/`Option` strings.

## Tests

New: `PlatformKeysTest` (no `Alt+Tab` and no `Ctrl+Alt+arrows` on Windows/Linux; zoom works without `=`; F1 lists only shortcuts that work on that platform), `UiFontsTest`.
Extended: `NoteEditorTest.codeSectionsUseAnInstalledMonospaceAndPlainTextAnInstalledFont`, and `ShortcutTextTest` now parses the help rows of both platforms.

## Not verifiable here

This audit ran on macOS. The Windows system look and the Linux font choice were checked in code and with headless Metal renders, not on real Windows/Linux machines.
