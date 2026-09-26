# cuteNotes: product review and improvement proposals

Date: 2026-09-26 · version `v2026-09-26-3` · branch `develop`

This review covers the code (about 8,500 lines, 104 classes in `view`, 189 tests), the current builds for three operating systems and the usage scenarios. The app is compared with products in its niche: Tot, Drafts, Bear, Apple Notes, Stickies, and the scratch tabs of Sublime and Notepad++.

---

## 1. Verdict: **57 / 100**

**Scale.** 100 is a paid product in this niche that people are happy to pay for: Tot, Drafts, Bear. 50 is a working, tidy free utility.

cuteNotes is currently a **reliable, good-looking scratchpad**. It does not lose data, it looks like a premium product, it runs on three operating systems, and its builds are made automatically. To become a product people want to buy, it lacks three things:

1. **Instant access.** A global hotkey from anywhere, an icon, a real presence in the system.
2. **Finding anything.** Search through the contents of all notes.
3. **Discoverability.** A new user sees an empty window and does not know that `Esc` and `F1` exist.

The good news: most items of the top 10 below take days, not months. The first wave of improvements realistically lifts the score to about 70; all three waves, to about 88 (section 9).

---

## 2. Positioning

| What it is | What it is not (and should not become) |
|---|---|
| A scratch window on top of everything else: open it, write, close it, lose nothing | A knowledge base with a link graph (Obsidian) |
| Keyboard control, tabs, plain files on disk | A collaborative editor (Notion) |
| Cross-platform, no accounts, no cloud | A full Markdown editor (Typora) |

Hence the main test for any feature: **does it speed up the path "thought → written down → found again"**. Anything that makes startup or the interface heavier goes against what the product is.

---

## 3. Score by area

| Area | Weight | Score (0–10) | Points | In short |
|---|---:|---:|---:|---|
| Reliability and data safety | 15 | 7.5 | 11.3 | Atomic writes, autosave, protection against a second instance. No trash, no version history, no backups |
| Editor and formatting | 20 | 5.0 | 10.0 | Bold, italic, code, images, IntelliJ-style line operations. No lists, headings, links, replace, context menu or drag & drop |
| Organization and search | 15 | 5.0 | 7.5 | Tabs, nested groups, quick switcher. Search by name only; no tags, pinning or sorting |
| Visual design | 10 | 7.5 | 7.5 | Theme presets, vector icons, animations. No app icon of its own; the title bar does not always match the theme |
| UX, onboarding, accessibility | 10 | 4.5 | 4.5 | The interface is hidden and nothing guides a newcomer. No localization; weak screen-reader support |
| OS integration | 10 | 3.5 | 3.5 | No global summon, no tray, no drag & drop of files, does not follow the system theme |
| Distribution and updates | 10 | 5.0 | 5.0 | CI releases and a bundled Java runtime. No signing or notarization, icon, installers, auto-update, or version shown in the app |
| Cross-platform | 5 | 7.0 | 3.5 | Shortcuts, fonts and look adapted to each OS. No builds for Intel Mac or ARM Linux/Windows |
| Engineering and process | 5 | 8.0 | 4.0 | Tests, checkstyle, CI. The UI is excluded from coverage; no Windows/macOS smoke tests; no logs |
| **Total** | **100** | | **56.8** | |

---

## 4. What is already good (keep it)

- **Data is never lost.** Atomic writes, autosave every 300 ms, a lock against a second instance, a clean exit; the packaged apps keep their data in `~/cutenotes_data`.
- **Plain files.** `note_<id>_<name>.txt` can be opened in any editor and found with grep. That trust and the absence of lock-in are a strong selling point.
- **Keyboard first.** IntelliJ-style line moves, caret history back/forward, `Cmd/Ctrl+R` as a quick switcher, F1 with key caps.
- **Visual design.** Design tokens (`UiPalette`), 6 theme presets, raised tabs, toast notifications, smooth animations.
- **Engineering.** 189 tests, checkstyle, CI building on three operating systems, screenshots rendered from the real components.

---

## 5. What is decidedly missing: top 10

| # | Missing | Why it matters | Effort |
|---|---|---|---|
| 1 | **Search through the contents of all notes** | "I know I wrote this down" is the main scenario for a notes app. `Cmd+R` currently searches names only | M |
| 2 | **Onboarding and discoverability** | First start: an empty window with no menu, tabs or hints. Without `Esc` and `F1` the features are simply invisible | S |
| 3 | **Instant summon** (show and hide the window from anywhere) | The point of a scratchpad is to write something down in 2 seconds. Launching again currently only says "already running" | M |
| 4 | **App icon, signing and notarization** | The default Java icon in the Dock and taskbar plus the Gatekeeper warning are the main "not premium" signals | S (icon) / M (signing) |
| 5 | **Lists and checkboxes that continue on Enter, headings** | To-do lists are the most common kind of note. Enter after `- ` should continue the list | M |
| 6 | **Trash and version history** | Deleting cannot be undone. `Cmd+A` plus typing over the text cannot be rolled back after a restart | S–M |
| 7 | **Clickable links and drag & drop** of files and images | A link cannot be opened, an image cannot be dragged in, `.txt`/`.md` cannot be dropped onto the window | S–M |
| 8 | **Find and replace** | A basic feature of any editor | S |
| 9 | **Text context menu and Edit/Format menus** | Formatting is only available through shortcuts; a mouse user cannot find it | S |
| 10 | **Version, "About", update check** | There is no way to see which version is installed or that a new one is out | S |

Effort: **S** up to a day, **M** 2–5 days, **L** 1–3 weeks.

---

## 6. Detailed review and proposals

For each item: problem → proposal → how to build it in the current architecture → priority (P0 first, P3 some day).

### 6.1 First start and discoverability

**Problem.** The menu and tabs are hidden by default (`Revealer`), and the empty window shows no hint at all. A newcomer learns about `Esc`, `F1` and `Cmd+R` only from the README. There is a single menu ("Menu"), and formatting is not in it.

| Proposal | How | Priority |
|---|---|---|
| A hint in the empty note: muted text "Start typing · ⌘T new tab · Esc menu · F1 all shortcuts" | Painted over an empty `NoteEditor` (like the placeholder in `HintField`), disappears with the first character | P0 · S |
| A welcome notice on first start and after an update: "What's new" | A `lastSeenVersion` flag in the options, a `Toast` or a small dialog | P1 · S |
| A `File / Edit / Format / View / Help` menu bar instead of a single "Menu", with formatting and its shortcuts in Format | Split `AppMenu` into menus, reuse the existing actions | P1 · S |
| A text context menu: Cut / Copy / Paste / Paste as plain text / Format ▸ / Select all | A popup in `NoteEditor`, like `ImageActions` (DRY with `EditorFormat`) | P0 · S |
| An optional formatting bar above selected text (as in Medium and Notion) | A `JWindow` or `JLayeredPane` above the selection, 5 `FlatButton`s | P3 · M |

### 6.2 Quick capture and OS integration

**Problem.** Java has no API for global hotkeys, and the project's policy is "no dependencies except Lombok". Launching the app again currently shows an "already running" dialog.

| Proposal | How | Priority |
|---|---|---|
| **Launching again shows the window that is already open.** The second instance signals the first (e.g. through a local socket on `127.0.0.1` with the port in `cutenotes_data/.port`), which comes to the front. An OS-level hotkey is then attached to launching: macOS Shortcuts or a shortcut, a shortcut key in Windows, a keybinding in GNOME/KDE. The result is a global summon without native dependencies | `InstanceLock` plus a small `ActivationServer` on a virtual thread; instructions for three operating systems in the README | P0 · M |
| "Hide" is the same hotkey pressed again (toggle) | A `toggle` signal instead of `show` | P1 · S |
| An icon in the tray or menu bar: show/hide, new note, quit | `SystemTray` (Windows/Linux, partly macOS) | P2 · M |
| **App icon** | Drawn as vectors (like `VectorIcon`), with `.icns`/`.ico`/`.png` generated by a script. `jpackage --icon`, and `Taskbar.setIconImage` plus `Window.setIconImages` at runtime for the jar | **P0 · S** |
| Dragging files onto the window: `.txt`/`.md` open as tabs, images are inserted | A `TransferHandler` for `NoteEditor` and `TabsPane`; reuse `NoteImages` for images | P1 · M |
| Follow the system light/dark mode (a pair: Paper ↔ Graphite) | Detect the OS theme without dependencies: macOS `defaults read -g AppleInterfaceStyle`, the `AppsUseLightTheme` registry value on Windows, `gsettings` on GNOME. Poll every N seconds | P2 · M |
| Window transparency when on top (for sticky notes) | `Window.setOpacity` works only for undecorated windows → needs a separate compact mode | P3 · L |

### 6.3 Editor

| Proposal | How | Priority |
|---|---|---|
| **Lists that continue.** Enter after `- `, `* `, `1. `, `- [ ] ` continues the list; Enter on an empty item ends it. `Tab`/`Shift+Tab` change the level | An Enter action in `LineOps`; line prefixes parsed with `Lines` | **P0 · S** |
| **Checkboxes** `- [ ]` / `- [x]`: toggled by a click or `Cmd/Ctrl+Enter` (currently "10 blank lines"; that binding should be revisited) | A click on the `[ ]` range in `NoteEditor`; stored as ordinary Markdown | P0 · S |
| **Headings** `#`, `##`: live preview, with the line larger and bold and the marker muted (as in Bear and Typora) | Paragraph attributes recomputed as the document changes. Important: no undo entries (a lesson from `CodeRestyle`) | P1 · M |
| **Clickable links**: underlined, opened with `Cmd/Ctrl+click` | Find URLs in the visible text with a regular expression, highlight them without editing the document, `Desktop.browse` | P0 · S |
| **Find and replace** (`Cmd/Ctrl+Alt+F` or `Cmd+Shift+H`), with "Replace all" as a single undo step | A second field in `SearchBar`, `EditorUndo.beginGroup` | P0 · S |
| Regular expressions and "whole word" in search | Toggles in `SearchControls`, logic in `SearchEngine` | P2 · S |
| Word and character count on request (in a notice or a status bar) | An action plus a `Toast` | P2 · S |
| Multi-line ```` ``` ```` code blocks (currently only inline `code`) | Extend `MarkdownText` with block nodes, a full-width background | P2 · M |
| Syntax highlighting in code blocks (a few languages) | A small tokenizer of our own (no dependencies) | P3 · L |
| A maximum line width, centered in a wide window (a comfortable line length) | `NoteEditor` padding based on the viewport width | P2 · S |
| "Paste as plain text" (`Cmd/Ctrl+Shift+V`) | `EditorClipboard` | P1 · S |
| Automatic titles: a new "untitled" tab takes its first line as its name until it is renamed by hand | `NoteSession`/`TabsPane`, a "named by hand" flag | P1 · S |
| Spell checking | The JDK has no API; it needs Hunspell dictionaries and a library (against the dependency policy) | P3 · L |

### 6.4 Search and organization

| Proposal | How | Priority |
|---|---|---|
| **Full-text search** in `Cmd+R`: text matches with a snippet of the line; Enter opens the note and puts the caret on the match | An in-memory index built from `TabRepository.load` (on a virtual thread when the browser opens; with hundreds of notes a plain linear scan is enough). `NoteFilter` searches by name **and** text, `NoteCellRenderer` shows the snippet | **P0 · M** |
| Tab switching: `Cmd/Ctrl+1…9`, previous tab with `Ctrl+Shift+Tab`, plus `Cmd+Shift+[ ]` (macOS) and `Ctrl+PgUp/PgDn` (Windows/Linux) | Bindings in `AppMenu` through `PlatformKeys`, `OpenTabs.previous()` | **P0 · S** |
| Pinned notes and sorting in the browser (by date / name / by hand) | `GroupData` plus a switch in `BrowserToolbar` | P2 · S |
| `#tags` in the text plus a tag filter in `Cmd+R` | Parsed while indexing, chips in the toolbar | P2 · M |
| A note or tab colour (the sticky-note metaphor) | A metadata field, a colour stripe on `TabChip` | P2 · S |
| Recent notes in the menu | `TabHistory` plus a submenu | P3 · S |

### 6.5 Data safety, sync, privacy

| Proposal | How | Priority |
|---|---|---|
| **Trash**: deleted notes are kept for 30 days in `cutenotes_data/trash` and can be restored from `Cmd+R` ("Recently deleted") | `FileTabRepository.delete` → move the file; clean up at startup | **P0 · S** |
| **Version history**: snapshots of a note on significant changes (at most every N minutes, the last K). Viewing and restoring | `AutoSaver` writes snapshots to `cutenotes_data/history/<id>/`; viewed in a dialog with `ThemePreview` | P1 · M |
| A daily backup: a zip of the data folder, the last 7 kept | A virtual thread at startup | P1 · S |
| **Choice of data folder** (iCloud Drive, Dropbox, Syncthing) | An option in Options; `DataDir` reads it from a file in the home folder | P1 · M |
| **Reloading notes changed on disk** (sync, an external editor). Today the app silently overwrites such changes | A `WatchService` on the data folder. If the tab was not changed, reload it; on a conflict, keep a "(conflict)" copy and show a notice | P1 · M (required together with the folder choice) |
| A lock across two machines through a shared folder (`FileChannel.lock` is local only) | A "lease" file with the hostname and time, plus a warning | P2 · M |
| **Encrypted notes** (password, AES-GCM, PBKDF2 — all in the JDK), auto-lock when idle. People really do keep keys and passwords in a notes app | An "encrypted" flag in the metadata, decryption in memory, a password dialog | P2 · M |
| Export a note to `.md`/`.txt`/`.html`, and print or PDF | `JTextComponent.print()` is in the JDK; HTML from `DocumentMarkdown` | P2 · S |

### 6.6 Visual design and typography (what is left)

| Problem / proposal | How | Priority |
|---|---|---|
| Line spacing is a fraction of the line height, so a large image gets a gap of about 15 % of its height below it | A custom `ParagraphView` in `NoteEditorKit` with a fixed gap, or images as their own paragraph without spacing | P2 · M |
| On macOS, after switching between a light and a dark theme, the title bar turns grey until a restart | Suggest a restart in a notice, or start with `appearance=system` and pick the title colour | P3 · S |
| Options keeps the native look while the other windows use the theme colours | A deliberate choice, but worth bringing in line: sections as cards, the preview in a window frame | P3 · M |
| Interface density: a compact/normal mode for small sticky windows | A density token in `UiPalette` / `TabPanes` | P3 · S |

### 6.7 Accessibility and localization

| Proposal | How | Priority |
|---|---|---|
| **Russian localization** (and a basis for other languages) | Strings in a `ResourceBundle` (`Messages_ru.properties`), language taken from the OS | P1 · M |
| Screen readers: accessible names and roles for `TabChip` and `FlatButton` (only a tooltip today), announced notices | `getAccessibleContext().setAccessibleName`, `AccessibleRole.PAGE_TAB` | P2 · S |
| Contrast of muted text (`UiPalette.muted`) of at least WCAG AA 4.5:1 in every preset | A test in `UiPaletteTest`; darken the blend where it falls short | P2 · S |
| Keyboard navigation of the tabs bar and toolbars (focus, arrows) | Focus and arrow keys in `TabHeader` | P3 · M |

### 6.8 Distribution, updates, monetization

| Proposal | How | Priority |
|---|---|---|
| **Icon** | See 6.2 | P0 · S |
| **Version in the app**: an "About" window (version, link to the release, data folder, licence) | The version is passed at build time (`-Dcutenotes.version` in `package.sh` and the jar manifest) | P0 · S |
| **Update check**: a request to `api.github.com/repos/.../releases/latest` (the JDK has `HttpClient`), a "Version X is available" notice, can be turned off in Options | A virtual thread at startup, once a day | P1 · S |
| **macOS signing and notarization**: removes the Gatekeeper warning. Needs an Apple Developer ID ($99/year); in CI, `codesign` plus `notarytool` with secrets | A step in `release.yml` for `macos-14` | P1 · M |
| Windows signing: removes SmartScreen (Azure Trusted Signing, about $10/month) | A CI step for `windows-latest` | P2 · M |
| Installers: `.dmg`, `.msi`, `.deb` (jpackage makes all three) | Extra `--type`s in `package.sh`, files in the release | P1 · S |
| More architectures: Intel Mac (`macos-13`), Linux ARM64 (`ubuntu-24.04-arm`), Windows ARM64 | Rows in the CI matrix | P1 · S |
| Package managers: Homebrew cask, winget, Flathub or AUR | Manifests plus automatic updates from CI | P2 · M |
| A product page (GitHub Pages): the screenshots are already generated automatically | A static site from the README and screenshots | P3 · M |
| Payment model: pay-what-you-want / GitHub Sponsors / a one-off "Pro" licence (encryption, version history, sync) | A product decision; technically the licence key is checked locally | P3 |

### 6.9 Engineering and process

| Problem / proposal | How | Priority |
|---|---|---|
| **Every push to `develop` is a public release and "Latest".** A typo fix in the README also becomes a release | A push to `develop` publishes a **pre-release**; "Latest" is given only to a tagged or manually approved release (`workflow_dispatch`). Alternatively, `paths-ignore` for `docs/**` and `*.md` | P1 · S |
| Coverage does not count `view/*`, which is most of the code | A separate threshold for `view` (there are already many headless tests) | P2 · S |
| Smoke tests exist for Linux only | Windows: start the `.exe` and check the process and data folder through PowerShell. macOS: start the `.app` with `open` on the runner (this works with an ad-hoc signature) | P1 · S |
| Screenshot tests: comparison with a reference to catch visual regressions | The infrastructure is already there (`ReadmeScreenshots`); needs a tolerant comparison and references | P2 · M |
| **No logs**: a user's complaint cannot be investigated | `java.util.logging` → `cutenotes_data/logs` with rotation; a "Show logs" menu item | P1 · S |
| Performance on large notes (1–5 MB, hundreds of images) has not been measured | A benchmark for opening, typing and saving; a budget, e.g. "open 1 MB in 300 ms" | P2 · M |

---

## 7. Known bugs and technical debt

| # | What | Risk | Priority · effort |
|---|---|---|---|
| 1 | Image paths from Markdown are not confined to the data folder (`![](../../x.png)`): "Open full image" opens any file | Security | **P0 · S** |
| 2 | Images in `cutenotes_data/images` are never deleted | The folder grows forever | P1 · S |
| 3 | Images are scaled with `getScaledInstance(SCALE_SMOOTH)`: about 19 ms per resize step | Stutter | P1 · S |
| 4 | External changes to files are silently overwritten (see 6.5) | Data loss with sync | P1 · M |
| 5 | The gap below images is proportional to their height | Visual | P2 · M |
| 6 | The version inside a build is computed in UTC (`26.9.25`) and does not match the tag (`2026-09-26`) | Confusion | P2 · S |
| 7 | Old files with unescaped `*` and `__` are shown with formatting | Rare | P3 · M |
| 8 | The macOS title bar after switching between a light and a dark theme | Visual | P3 · S |
| 9 | `Cmd/Ctrl+Enter` (10 blank lines) takes a key usually used for checkboxes and sending | UX | P2 · S |

---

## 8. What not to do

- **Accounts and a cloud of our own.** That contradicts "plain files, no lock-in". Sync through a chosen folder is enough.
- **A knowledge base** (graph, backlinks, plugins). That is a different product; every such feature makes startup heavier.
- **Built-in AI.** It does not solve the scratchpad's core task and brings dependencies and network use.
- **Heavy WYSIWYG** with tables and nested blocks. A live preview of lists and headings gives 90 % of the value for 10 % of the cost.

---

## 9. Roadmap

| Wave | Contents | Time | Score after |
|---|---|---|---:|
| **1. Quick wins** | Empty-window hint, context menu, icon, "About" with the version, `Cmd/Ctrl+1…9` and previous tab, lists and checkboxes, links, find and replace, trash, image paths (bug #1) | ~1.5–2 weeks | **~70** |
| **2. The core of the product** | Full-text search, instant summon (activation plus an OS hotkey), drag & drop, headings, version history and backups, update check, logs, installers and Intel/ARM builds, Windows/macOS smoke tests, a pre-release on every push | ~3–4 weeks | **~80** |
| **3. Premium** | Notarization and signing, data-folder choice with reloading and conflicts, encryption, localization, system theme, tags, pinning, export and print, accessibility, package managers | ~4–6 weeks | **~88** |

After the first two waves, cuteNotes competes fairly with Tot and Stickies for the "scratch window on top of everything" niche and wins on cross-platform support and plain files. The third wave moves it into the "happy to pay for it" category.

---

## 10. Limits of the current "no dependencies" policy

| Feature | Without dependencies | With a dependency |
|---|---|---|
| Global hotkey | An OS shortcut plus activation of the running instance (6.2) — good enough | JNativeHook / JNA: a real hook, but native code on three operating systems |
| Spell checking | Not possible | Hunspell or JLanguageTool (large dictionaries) |
| Syntax highlighting | Our own tokenizer for a few languages | RSyntaxTextArea (but that is a different editor component) |
| System theme | OS commands through `ProcessBuilder` — works | FlatLaf / JNA — more precise, but it means changing the LAF |

Recommendation: keep the policy. Everything in sections 5 and 9 can be built without new dependencies.
