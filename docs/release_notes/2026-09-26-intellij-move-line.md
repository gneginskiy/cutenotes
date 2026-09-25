# cuteNotes: IntelliJ-style "move line up / down"

Date: 2026-09-26 · branch `develop` · `mvn clean install`: all 185 tests pass, 0 checkstyle violations, coverage above the threshold.

## Shortcut

| macOS | Windows / Linux |
|-------|-----------------|
| `Cmd+Shift+↑` / `Cmd+Shift+↓` | `Ctrl+Shift+↑` / `Ctrl+Shift+↓` |

The modifier comes from `ShortcutMask.menu()`. A test installs the bindings with both masks and checks each of them.

## Why it "jumped"

| # | Problem | Fix |
|---|---------|-----|
| 1 | Both lines were deleted and then re-inserted. The document briefly got shorter, and when the view was scrolled near the end, the scroll position was clamped and never came back | The neighbouring line is copied to the other side of the block first, and only then is the original removed. The document never gets shorter, and the moved lines' own text and styles are not touched |
| 2 | Every intermediate caret position scrolled the view | The caret is frozen (`DefaultCaret.NEVER_UPDATE`) during the edit. Afterwards the view position is restored and scrolled only as much as needed to keep the moved line visible |
| 3 | One move took 3–4 undo steps, so `Cmd/Ctrl+Z` undid it in pieces | One undo step (`EditorUndo.beginGroup/endGroup`). Duplicate line (`Cmd/Ctrl+D`) is now a single step as well |
| 4 | The selection was lost, and only the caret line moved | Every line the selection touches moves as one block and stays selected, including its direction. As in IntelliJ, a selection that ends at the start of a line leaves that line where it is |
| 5 | Moving a long line (over 40 characters) was recorded as a navigation jump, so "Back" (`Cmd+Option+←` / `Alt+←`) went to a strange place | Caret moves caused by the edit do not enter the history (`CaretHistory.quietly`) |

Edge cases that are handled: the last line without a trailing newline (in both directions), the empty line after a trailing newline, empty lines, and nothing happening at the top and bottom of the document.

## Classes

- `LineMove` (new): pure plan over plain text (block, neighbour, what to insert and remove, how far the selection shifts).
- `LineMover` (new): applies the plan to the editor (styles and images via `StyledRuns`, undo, caret, viewport).
- `LineOps`: bindings only; `install(area, undo, mask)`. The old `moveLine` and the now unused `StyledRuns.trimTrailingNewline` were removed.

## Tests

- `LineMoveTest` (8 tests): up / down, caret column, last line, document edges, multi-line block with selection, a selection ending at the start of a line, the empty last line, empty lines.
- `LineOpsTest` additions:
  - `Cmd+Shift` on macOS and `Ctrl+Shift` on Windows / Linux;
  - selection, bold and a single undo step;
  - the view does not scroll;
  - "Back" is not polluted.
- Written first: three of them failed on the old implementation (selection/undo, key mask, navigation history). The scroll test also passed on the old code in headless mode and stays as a regression guard.
