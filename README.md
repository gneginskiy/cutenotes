# cutenotes

A minimalist keyboard-driven cross-platform (Linux, Windows, macOS) always-on-top scratchpad with auto-saved tabs,
written in plain Java w/o dependencies (except Lombok, it doesn't count).
Stores everything in plain text (Markdown). Customizable colors, fonts, and other settings.

- Tabs that save themselves; a quick switcher and full-text search across all notes, `#tags`, pinned and coloured notes
- Lists, checkboxes, headings, links, bold / italic / code, pasted or dropped pictures — all stored as plain Markdown
- Find & replace (match case, whole words, regular expressions)
- Nothing gets lost: version history per note, "Recently deleted" for 30 days, a daily backup, reload or keep-both when a synced file changes
- Password-protected notes (AES-256), locked again after idle time
- Export to Markdown, text or a web page; print
- English and Russian; follows the system's light / dark mode; tray icon; notes folder can live in Dropbox / iCloud Drive

<div align="center">
<table>
  <tr>
    <td><a href="screenshots/img4.png"><img src="screenshots/img4.png" width="300" alt="cuteNotes screenshot 4"></a></td>
    <td><a href="screenshots/img2.png"><img src="screenshots/img2.png" width="300" alt="cuteNotes screenshot 2"></a></td>
    <td><a href="screenshots/img6.png"><img src="screenshots/img6.png" width="300" alt="cuteNotes screenshot 6"></a></td>
  </tr>
  <tr>
    <td><a href="screenshots/img5.png"><img src="screenshots/img5.png" width="300" alt="cuteNotes screenshot 5"></a></td>
    <td><a href="screenshots/img3.png"><img src="screenshots/img3.png" width="300" alt="cuteNotes screenshot 3"></a></td>
    <td><a href="screenshots/img.png"><img src="screenshots/img.png" width="300" alt="cuteNotes screenshot 1"></a></td>
</tr>
</table>
</div>

<p align="center"></p>
<p align="center"></p>

## Getting started

Download the build for your system below: it ships with its own Java runtime, nothing else to install.
Or, with Java 21+ installed, download the jar and double-click it.

## Download

The latest build, straight from [GitHub Releases](https://github.com/gneginskiy/cutenotes/releases/latest):

| System | Download | Installer | Start |
|--------|----------|-----------|-------|
| macOS (Apple silicon) | [cutenotes-mac-arm64.zip](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-mac-arm64.zip) | [.dmg](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-mac-arm64.dmg) | Unzip, move `cuteNotes.app` to Applications, open it (first start: System Settings → Privacy & Security → Open Anyway) |
| macOS (Intel) | [cutenotes-mac-x64.zip](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-mac-x64.zip) | [.dmg](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-mac-x64.dmg) | Same as Apple silicon |
| Windows x64 | [cutenotes-windows-x64.zip](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-windows-x64.zip) | [.msi](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-windows-x64.msi) | Unzip, run `cuteNotes\cuteNotes.exe` |
| Linux x64 | [cutenotes-linux-x64.tar.gz](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-linux-x64.tar.gz) | [.deb](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-linux-x64.deb) | `tar -xzf`, run `cuteNotes/bin/cuteNotes` |
| Linux arm64 | [cutenotes-linux-arm64.tar.gz](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-linux-arm64.tar.gz) | [.deb](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes-linux-arm64.deb) | Same as Linux x64 |
| Any OS with Java 21+ | [cutenotes.jar](https://github.com/gneginskiy/cutenotes/releases/latest/download/cutenotes.jar) | | Double-click, or `java -jar cutenotes.jar` |

macOS Intel, Linux arm64 and the installers are built when their CI runners manage; if a link is missing, take the archive.

Every version is on the [releases page](https://github.com/gneginskiy/cutenotes/releases).
Jars of releases before 2026-09-26-3 are kept in [releases/](releases).

## How it works

The apps keep your notes, session and options in `cutenotes_data/` in your home folder, so updating the app keeps them.
The plain jar is portable: its `cutenotes_data/` folder sits next to the jar.
Moving from the jar to an app? Copy that folder into your home folder.
To keep notes in Dropbox, iCloud Drive and the like, pick the folder in Options → Application → Notes folder;
cuteNotes warns when another computer is using the same folder at the moment.

Inside `cutenotes_data/`: one `.txt` file per note (Markdown), `images/`, `history/` (earlier versions of each note),
`trash/` (deleted notes, kept 30 days), `backups/` (a zip a day, the last 7) and `logs/`.

**Summon the window with a hotkey:** starting cuteNotes while it runs brings its window to the front (the tray icon
does the same). Bind a system shortcut to start it — macOS: a Shortcuts action "Open App"; Windows: the "Shortcut key"
field in the properties of a shortcut to `cuteNotes.exe`; Linux: your desktop's keyboard settings.

## Shortcuts

Press **F1** in the app for the full, OS-aware list. Highlights:

| Action                      | macOS                         | Win / Linux                     |
|-----------------------------|-------------------------------|---------------------------------|
| New / Close tab             | `Cmd+T` / `Cmd+W`             | `Ctrl+T` / `Ctrl+W`             |
| Reopen last closed          | `Cmd+Shift+T`                 | `Ctrl+Shift+T`                  |
| All notes: search & open    | `Cmd+R`, type, `Enter`        | `Ctrl+R`, type, `Enter`         |
| Close tab with the mouse    | Middle-click or hover ×       | Middle-click or hover ×         |
| Next tab                    | `Option+Tab` or `Ctrl+Tab`    | `Ctrl+Tab`                      |
| Find / Replace              | `Cmd+F` / `Cmd+Option+F`      | `Ctrl+F` / `Ctrl+H`             |
| Find in all notes           | `Cmd+Shift+F`                 | `Ctrl+Shift+F`                  |
| Continue a list / indent    | `Enter` / `Tab`, `Shift+Tab`  | `Enter` / `Tab`, `Shift+Tab`    |
| Toggle checkbox             | `Cmd+Enter` or click          | `Ctrl+Enter` or click           |
| Export / Print note         | `Cmd+Shift+E` / `Cmd+P`       | `Ctrl+Shift+E` / `Ctrl+P`       |
| Lock protected notes        | `Cmd+Shift+L`                 | `Ctrl+Shift+L`                  |
| Options                     | `Cmd+O`                       | `Ctrl+O`                        |
| Zoom in / out text          | `Cmd+=` / `Cmd+-`             | `Ctrl+=` / `Ctrl+-` (or numpad `+`/`-`) |
| Undo / Redo                 | `Cmd+Z` / `Cmd+Shift+Z`       | `Ctrl+Z` / `Ctrl+Shift+Z`       |
| Cut / copy / duplicate line | `Cmd+X` / `Cmd+C` / `Cmd+D`   | `Ctrl+X` / `Ctrl+C` / `Ctrl+D`  |
| Move line up / down         | `Cmd+Shift+↑` / `Cmd+Shift+↓` | `Ctrl+Shift+↑` / `Ctrl+Shift+↓` |
| Back / forward to last spot | `Cmd+Option+←` / `Cmd+Option+→` | `Alt+←` / `Alt+→`             |
| Bold / Italic / Underline   | `Cmd+B` / `Cmd+I` / `Cmd+U`   | `Ctrl+B` / `Ctrl+I` / `Ctrl+U`  |
| Strikethrough               | `Cmd+Shift+S`                 | `Ctrl+Shift+S`                  |
| Code (monospace)            | `Cmd+Shift+C`                 | `Ctrl+Shift+C`                  |
| Paste image                 | `Cmd+V`                       | `Ctrl+V`                        |
| Show menu & tabs bar / Help | `Esc` / `F1`                  | `Esc` / `F1`                    |

## Build

```
mvn clean install
```

Output: `target/cutenotes.jar`. `scripts/package.sh` then packages the app with a bundled runtime for
the OS you run it on (into `target/dist/`); with `INSTALLERS=1` it also builds the `.dmg`, `.msi` or `.deb`.

Every push to `develop` is built, tested and packaged for macOS, Windows and Linux by
[GitHub Actions](.github/workflows/release.yml) and published as a GitHub Release; binaries are not
committed to the repository.

# Release notes
2026-Sep-26-4 (details in [docs/release_notes](docs/release_notes/2026-09-26-product-review-fixes.md)):
- Nothing gets lost: "Recently deleted" (30 days), version history per note, a daily backup, reload or keep-both when a synced file changes, a warning when another computer uses the notes folder
- Password-protected notes (AES-256) with auto-lock; export to Markdown / text / web page and print
- Lists and checkboxes that continue on Enter, headings, clickable links, `#tags`, find & replace, full-text search with snippets in `Cmd/Ctrl+R`, pinned and coloured notes
- Starting the app again shows / hides the window (bind any OS hotkey to it), tray icon, drag & drop of text files and pictures
- Options → Application: language (English / Russian), follow the system's light / dark mode, line width, updates, auto-lock, notes folder (e.g. in Dropbox)
- About window, update check, logs, app icon; installers (.dmg / .msi / .deb) and builds for macOS Intel and Linux arm64
- Readable secondary text (WCAG AA) in every theme; screen-reader names for buttons and tabs
2026-Sep-26-3 (details in [docs/release_notes](docs/release_notes/2026-09-26-github-releases.md)):
- Downloads now live on [GitHub Releases](https://github.com/gneginskiy/cutenotes/releases), built by GitHub Actions on every push to `develop`, natively on macOS, Windows and Linux
- Windows starts with `cuteNotes.exe` (no more `.bat`); Linux with `cuteNotes/bin/cuteNotes`
- Binaries are no longer stored in git; the platform archives were removed from the history

2026-Sep-26-2 (details in [docs/release_notes](docs/release_notes/2026-09-26-screenshots-and-text-layout.md)):
- Long words without spaces (keys, hashes, URLs) now wrap instead of scrolling the note sideways
- Even line rhythm: the first line of a note gets the same line spacing as the rest
- New screenshots of the current version

2026-Sep-26-1 (details in [docs/release_notes](docs/release_notes/2026-09-26-native-builds.md)):
- Downloads for macOS (Apple silicon), Windows x64 and Linux x64 that bundle their own Java runtime: no JDK needed
- The apps keep notes in `~/cutenotes_data`, so updating an app never deletes them; the plain jar stays portable
- One script built the jar and all three native downloads (replaced by GitHub Actions in 2026-Sep-26-3)

2026-Sep-26 (details in [docs/release_notes](docs/release_notes)):
- Six theme presets in Options (Paper, Graphite, Midnight, Sepia, Sticky, Terminal); new default "Paper" look
- Redesigned tabs: raised active tab, close on hover or middle-click, "+" button, Close other tabs
- `Cmd/Ctrl+R` is a quick switcher: type to filter, `Enter` to open; relative dates and icons
- Roomier editor (padding, line spacing), themed selection and scrollbars, rounded find bar
- Key-cap help (F1), fading status toasts, smooth animations, remembered window size and position
- Move line up / down (`Cmd/Ctrl+Shift+↑/↓`) works like IntelliJ: moves the whole selection as a block, keeps it selected, no view jumps, one undo step
- Cross-platform: native look on Windows, platform-safe shortcuts (`Ctrl+Tab`, `Alt+←/→` on Windows/Linux), zoom with `Ctrl+=`/`Ctrl+-` or numpad on any keyboard layout, installed monospace fonts for code
- Reliability fixes from the critical bug review (autosave, atomic writes, single instance, clean exit)

2026-Jun-05:
- Back / forward caret navigation across visited spots (`Cmd/Ctrl+Alt+←/→`), like an IDE
- Configurable code-section colour with an auto, theme-aware background
- Rich text: **bold**, *italic*, underline, ~~strikethrough~~ and `code` (`Cmd/Ctrl+B/I/U`, `Cmd/Ctrl+Shift+S`, `Cmd/Ctrl+Shift+C`), stored as Markdown
- Paste images from the clipboard (`Cmd/Ctrl+V`); saved in `cutenotes_data/images`, resize or remove via right-click, move by dragging
- Nested groups in the `Cmd/Ctrl+R` window: drag notes/groups to regroup, reorder notes, rename inline, right-click menu, delete with confirmation
- Pin button (📌) to keep the tabs bar visible

2026-Jun-03:
- Case-insensitive search (toggle for case-sensitive)
- Forward / backward buttons in the search bar
- Note groups (folders) in the `Cmd/Ctrl+R` window
- `Cmd/Ctrl+Enter` inserts 10 blank lines

## License

MIT — see [LICENSE](LICENSE). Free to copy, modify, and redistribute; the copyright notice and license text must be kept in any copies or substantial portions.

Author: **Grigorii Neginskii**.

