# cuteNotes: new README screenshots, text layout fixes

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 189 tests pass, 0 checkstyle violations, coverage above the threshold.

## Screenshots

`screenshots/img*.png` were re-rendered: the same six scenes with the same text, in the new version of the app.

| File | Scene |
|------|-------|
| `img.png` | Black and green "Wake up, Neo…" note (Courier New) |
| `img2.png` | Light-blue decision record (Comic Sans), window inactive |
| `img3.png` | Options with the pink theme (`#FFBDBD` / `#000000` / caret `#004242`), now with the theme presets |
| `img4.png` | Yellow sticky with the "various stuff" / "security_training" tabs |
| `img5.png` | The new two-column F1 sheet with key caps |
| `img6.png` | The **current** README pasted into a black and orange monospace note |

`scripts/update-screenshots.sh` renders them from the real UI components of the app (`scripts/screenshots/view/ReadmeScreenshots.java`) inside a drawn macOS window frame at 2× resolution. The frame has a title bar tinted in the theme's colour, the window buttons and a shadow. Nothing appears on screen, and no notes are touched. The native Aqua look needs a connection to the macOS window server, so the script runs without `java.awt.headless`, with `apple.awt.UIElement` set to hide the Dock icon.

## Bugs found while making the screenshots

Each fix started with a test that reproduces the bug.

| # | Problem | Fix | Test |
|---|---------|-----|------|
| 1 | A word longer than the line (SSH key, hash, URL) did not wrap. The note scrolled sideways and the text was cut off. Swing's styled text only breaks at spaces; the old `JTextArea` did wrap such words | The note editor's `NoteEditorKit` uses a text view with zero minimum width, so the paragraph breaks it at any character when no space fits. Normal words still wrap at spaces | `NoteEditorTest.aLongWordWithoutSpacesWrapsInsteadOfScrollingSideways` |
| 2 | The first line of every note had no line spacing (21 px against 24 px in Comic Sans 14), so the rhythm was uneven. The spacing was set on the style of a still empty document, and Swing does not refresh existing views after such a change | The spacing is set in `NoteEditorKit.createDefaultDocument()`, before any views exist. The Options preview uses the same kit, so it shows exactly what a note will look like. The late setting in `EditorTheme` was removed | `NoteEditorTest.theFirstLineGetsTheSameLineSpacingAsTheRest` (note editor and preview) |

New class: `NoteEditorKit`. It is attached through `createDefaultEditorKit()`, so the document, the undo history and the Markdown cache are created on the right kit from the start.

Released in `2026-09-26-2` (jar and native builds).
