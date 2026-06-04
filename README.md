# cutenotes

A minimalist keyboard-driven cross-platform (Linux, Windows, macOS) always-on-top scratchpad with auto-saved tabs,
written in plain Java w/o dependencies (except Lombok, it doesn't count).
Stores everything in plain text. Customizable colors, fonts, and other settings.

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

Install Java 21+, download the release jar below, double-click on it.

## Download
[cutenotes-2026-06-05.jar](releases/cutenotes-2026-06-05.jar) - the latest release

## How it works

A `cutenotes_data/` folder is created next to the jar. It holds your notes, session, and options.

## Shortcuts

Press **F1** in the app for the full, OS-aware list. Highlights:

| Action                      | macOS                         | Win / Linux                     |
|-----------------------------|-------------------------------|---------------------------------|
| New / Close tab             | `Cmd+T` / `Cmd+W`             | `Ctrl+T` / `Ctrl+W`             |
| Reopen last closed / picker | `Cmd+Shift+T` / `Cmd+R`       | `Ctrl+Shift+T` / `Ctrl+R`       |
| Next tab                    | `Option+Tab` or `Ctrl+Tab`    | `Ctrl+Tab`                      |
| Find                        | `Cmd+F`                       | `Ctrl+F`                        |
| Options                     | `Cmd+O`                       | `Ctrl+O`                        |
| Zoom in / out text          | `Cmd+Shift+=` / `Cmd+Shift+-` | `Ctrl+Shift+=` / `Ctrl+Shift+-` |
| Undo / Redo                 | `Cmd+Z` / `Cmd+Shift+Z`       | `Ctrl+Z` / `Ctrl+Shift+Z`       |
| Cut / copy / duplicate line | `Cmd+X` / `Cmd+C` / `Cmd+D`   | `Ctrl+X` / `Ctrl+C` / `Ctrl+D`  |
| Move line up / down         | `Cmd+Shift+↑` / `Cmd+Shift+↓` | `Ctrl+Shift+↑` / `Ctrl+Shift+↓` |
| Bold / Italic / Underline   | `Cmd+B` / `Cmd+I` / `Cmd+U`   | `Ctrl+B` / `Ctrl+I` / `Ctrl+U`  |
| Strikethrough               | `Cmd+Shift+S`                 | `Ctrl+Shift+S`                  |
| Code (monospace)            | `Cmd+Shift+C`                 | `Ctrl+Shift+C`                  |
| Paste image                 | `Cmd+V`                       | `Ctrl+V`                        |
| Toggle status bar / Help    | `Esc` / `F1`                  | `Esc` / `F1`                    |

## Build

```
mvn clean install
```

Output: `target/cutenotes.jar`.

# Release notes
2026-Jun-05:
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

