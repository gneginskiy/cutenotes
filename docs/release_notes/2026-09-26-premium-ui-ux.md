# cuteNotes: premium UI/UX redesign

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 166 tests pass, 0 checkstyle violations, coverage above the threshold.

The goal: make the app look and behave like a polished paid product on macOS, Windows and Linux, with no new dependencies (still plain Swing + Lombok).

## Summary

| # | Area | Before | After |
|---|------|--------|-------|
| 1 | Colours | Every bar computed its own shade (`darken(bg, 18)`, `contrast(0.12)`) | One set of design tokens (`UiPalette`) derived from the theme: chrome, hover, hairline, muted text, accent, selection, danger |
| 2 | Default look | Comic Sans on canary yellow | "Paper": warm off-white, graphite text, teal accent, the best installed UI font (SF Pro / Helvetica Neue / Segoe UI / Noto Sans…) |
| 3 | Themes | Only a raw HSV colour picker | Six one-click presets in Options: Paper, Graphite, Midnight, Sepia, Sticky (the classic look), Terminal |
| 4 | Tabs | Flat labels; bold active tab shifted its neighbours; 📌 emoji | Active tab is a raised card with an accent line that flows into the editor; × on hover; middle-click closes; "+" button; double-click on the empty bar opens a tab; right-click: Rename / Close / Close other tabs; the active tab scrolls into view |
| 5 | Icons | Emoji and text glyphs (`📌 ˄ ˅ ✕`) that looked different on every OS | Crisp vector icons (`VectorIcon`) and flat hover buttons (`FlatButton`) |
| 6 | Editor | Text glued to the window edges, default caret, blue OS selection, OS scrollbars (white track on dark themes) | 18/12 px padding, 15 % line spacing, 2 px caret, accent-tinted selection, thin themed scrollbars |
| 7 | Find | Plain field and bevelled buttons | Rounded field with a magnifier, placeholder and "1 of 5" counter; the outline turns accent on focus and red on "No results"; match colours adapt to the theme (were invisible on yellow) |
| 8 | Feedback | Zoom, pin and "reopen" gave no visible response | A fading HUD toast: "Text size 18 pt", "Tabs bar pinned", "No recently closed tabs" |
| 9 | Motion | Linear 3 px steps | Time-based ease-out (180 ms) for the tabs bar and menu |
| 10 | Cmd/Ctrl+R | Tree with `d MMM HH-mm` text | Quick switcher: type to filter (groups that lead to a match stay), Enter opens the best match and closes, ↓ moves into the list, typing in the list continues the search; note/folder icons, relative dates ("5 min ago", "Yesterday", "12 Sep"), open notes marked in accent |
| 11 | F1 help | Grey plain table, very tall | Two themed columns, shortcuts drawn as key caps; macOS shows `⌘ ⇧ ⌥ ⌃ ↩ ⇥` |
| 12 | Window | Always 500×400 in the centre | Size and position are remembered; bounds on an unplugged monitor are ignored |
| 13 | macOS title bar | Grey, unrelated to the theme | Tinted with the theme's chrome colour; dark themes get light title text |
| 14 | Zoom | 9 coarse levels (14 → 18 → 24) | 17 levels (…14, 15, 16, 18, 20…); sizes typed in Options snap in the zoom direction |

## How

- **Design tokens.** `UiPalette.of(Theme)` is the only place that derives UI colours; the menu, tabs, search bar, help, notes browser and toast all read from it. `Colors.isDark` uses perceived brightness.
- **Fonts.** `FontFamilies.resolve` falls back to the best installed UI font instead of the alphabetically first family. Previously a missing font silently became e.g. "Abadi", and saving Options made that permanent. Presets carry a font preference list (`ThemePreset.firstInstalled`).
- **Line spacing** lives on the document's default style, so it is neither an undoable edit nor part of the saved Markdown.
- **Tabs.** `TabChip` (one tab), `TabCard` (painting), `TabChipMouse` (gestures), `TabMenu` (context menu). `OpenTabs` took the bookkeeping out of `TabsPane` (SRP, and `TabsOps` was merged into it). `NoteSession.close(id)` / `closeOthers(id)` handle any tab, not just the active one, with the same save/delete rules as `Cmd+W`.
- **Notes browser.** `NoteFilter` + `GroupNodes.root(…, filter, …)`; `BrowserToolbar` and `BrowserTreeKeys` were split out of the dialog. Reordering by drag is paused while a search is active (it would reorder against hidden notes).
- **Platform.** `PlatformLook.prepare` runs before AWT starts: macOS appearance matching the theme, and LCD text antialiasing on Windows/Linux. If you switch between a light and a dark theme at runtime, the native title bar is shown until the next start (the appearance can only be set once).
- **Window bounds** are stored in `cutenotes_window.txt` (`FileBoundsStore`, atomic write; a failure never blocks quitting). A maximised window keeps its previous normal bounds.
- **DRY.** Duplicated tree-node lookups were moved into `GroupNodes.userObject`, and `InlineEditor` now uses `DocumentChanges`. The static mutable colours `TabHeader.ACTIVE/INACTIVE` and `Colors.darken/divider` were removed.

## Tests

New: `ThemePresetTest`, `FileBoundsStoreTest`, `UiPaletteTest`, `EasingTest`, `ToastFadeTest`, `ShortcutTextTest` (including a check that every help row parses), `RelativeTimeTest`, `GroupNodesTest`, `FontFamiliesTest`, `WindowPlacementTest`, `ZoomTest`, `VectorIconTest`, `SearchFieldTest`.
Extended: `NoteSessionTest` (closing an inactive tab from the bar saves it and keeps the active tab; close other tabs; "+" and rename from the bar; the reopen result; the bar hides after the last tab), `NoteEditorTest` (typography adds no undo edits and does not reach the Markdown; missing-font fallback).

## New classes

`model`: `ThemePreset`, `ThemePresets` · `dao`: `BoundsStore`, `FileBoundsStore` · `view`: `UiPalette`, `UiFonts`, `VectorIcon`, `IconShapes`, `FlatButton`, `TabChip`, `TabCard`, `TabChipMouse`, `TabMenu`, `TabRequests`, `OpenTabs`, `Toast`, `ToastFade`, `Easing`, `SearchField`, `HintField`, `AppMenu`, `HelpSections`, `KeyCaps`, `ShortcutText`, `NoteFilter`, `RelativeTime`, `BrowserToolbar`, `BrowserTreeKeys`, `PresetRow`, `PresetSwatch`, `OptionsFields`, `ThemePreview`, `WindowMemory`, `PlatformLook`. Removed: `Help` (now a menu item, F1), `TabsOps`.

## Compatibility

Existing `cutenotes_options.txt` files keep their colours and font. Only new installs and "Reset to default" get the Paper look. The classic yellow is one click away as the "Sticky" preset.
